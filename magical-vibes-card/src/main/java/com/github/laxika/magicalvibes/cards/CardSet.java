package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HuntedWumpus;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum CardSet {

    TENTH_EDITION("10E", "Tenth Edition", List.of(
            new CardPrinting("10E", "380", Forest::new),
            new CardPrinting("10E", "381", Forest::new),
            new CardPrinting("10E", "382", Forest::new),
            new CardPrinting("10E", "383", Forest::new),
            new CardPrinting("10E", "187", GiantSpider::new),
            new CardPrinting("10E", "246", GrizzlyBears::new),
            new CardPrinting("10E", "178", HuntedWumpus::new),
            new CardPrinting("10E", "148", Hurricane::new),
            new CardPrinting("10E", "268", LlanowarElves::new),
            new CardPrinting("10E", "270", MightOfOaks::new)
    ));

    private final String code;
    private final String name;
    private final List<CardPrinting> printings;

    public CardPrinting findByCollectorNumber(String collectorNumber) {
        return printings.stream()
                .filter(p -> p.collectorNumber().equals(collectorNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No printing with collector number " + collectorNumber + " in set " + code));
    }
}
