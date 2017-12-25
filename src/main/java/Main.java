import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class Main {
	public static Random random = new Random();
	public static void main(String[] args) throws IOException {
		List<Data> data = loadData();
		data = average(data);
		System.out.println(data.size());
		data = addFalseData(data);
		data = shovene(data);
		System.out.println(data.stream().count());

	}

	private static List<Data> shovene(List<Data> data) {
		NormalDistribution distribution = new NormalDistribution();
		//удаление по power1
		while(true) {
			Data av = averageElement(data);
			Data sd = standardDeviation(data);
			if(sd.getPower1() == 0)
				break;
			if(new NormalDistribution(av.getPower1(), sd.getPower1()).density(data.get(0).getPower1())*data.size() < 0.5) {
				data.remove(0);
			} else {
				break;
			}
		}
		//по power2
		while(true) {
			Data av = averageElement(data);
			Data sd = standardDeviation(data);
			if(sd.getPower2() == 0)
				break;
			data.sort((a, b) -> (int)(Math.abs(b.getPower2()-av.getPower2()) - Math.abs(a.getPower2()-av.getPower2())));
			if(new NormalDistribution(av.getPower2(), sd.getPower2()).density(data.get(0).getPower2())*data.size() < 0.5) {
				data.remove(0);
			} else {
				break;
			}
		}
		return data;
	}

	private static List<Data> addFalseData(List<Data> data) {
		Data av = averageElement(data);
		Data sd = standardDeviation(data);
		data.addAll(IntStream
				.range(0, 5)
				.mapToObj(p -> new Data("false",
						av.getPower1() + sd.getPower1() * 0,
						av.getPower2() + sd.getPower2() * 40))
				.collect(Collectors.toList()));
		return data;
	}

	private static Data standardDeviation(List<Data> data) {
		Data av = averageElement(data);
		double sdSquare1 = data.stream()
				.flatMapToDouble(p -> DoubleStream.of(p.getPower1()))
				.map(p -> p - av.getPower1())
				.map(p -> Math.pow(p, 2))
				.sum();
		double sdSquare2 = data.stream()
				.flatMapToDouble(p -> DoubleStream.of(p.getPower2()))
				.map(p -> p - av.getPower2())
				.map(p -> Math.pow(p, 2))
				.sum();

		return new Data("sd",
				Math.sqrt(sdSquare1 /(data.size()-1.)),
				Math.sqrt(sdSquare2 /(data.size()-1.)));
	}

	private static Data averageElement(List<Data> data) {
		double averagePower1 = data.stream()
				.filter(p -> p.getPower1() != null)
				.flatMapToDouble(p -> DoubleStream.of(p.getPower1()))
				.average().orElse(0);
		double averagePower2 = data.stream()
				.filter(p -> p.getPower2() != null)
				.flatMapToDouble(p -> DoubleStream.of(p.getPower2()))
				.average().orElse(0);
		return new Data("average", averagePower1, averagePower2);
	}

	private static List<Data> average(List<Data> data) {
		Data averageData = averageElement(data);
		return data
				.stream()
				.peek(p -> {
					if (p.getPower1() == null)
						p.setPower1(averageData.getPower1());
					if (p.getPower2() == null) {
						p.setPower2(averageData.getPower2());
					}
				})
				.collect(Collectors.toList());
	}

	private static List<Data> loadData() throws IOException {
		return new ObjectMapper().readValue(new File("data.json"), new TypeReference<List<Data>>() {
		});

	}
}
