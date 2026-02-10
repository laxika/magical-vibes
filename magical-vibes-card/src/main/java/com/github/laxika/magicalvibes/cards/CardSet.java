package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HuntedWumpus;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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
            new CardPrinting("10E", "270", MightOfOaks::new),
            new CardPrinting("10E", "376", Mountain::new),
            new CardPrinting("10E", "377", Mountain::new),
            new CardPrinting("10E", "378", Mountain::new),
            new CardPrinting("10E", "379", Mountain::new),
            new CardPrinting("10E", "368", Island::new),
            new CardPrinting("10E", "369", Island::new),
            new CardPrinting("10E", "370", Island::new),
            new CardPrinting("10E", "371", Island::new),
            new CardPrinting("10E", "364", Plains::new),
            new CardPrinting("10E", "365", Plains::new),
            new CardPrinting("10E", "366", Plains::new),
            new CardPrinting("10E", "367", Plains::new),
            new CardPrinting("10E", "372", Swamp::new),
            new CardPrinting("10E", "373", Swamp::new),
            new CardPrinting("10E", "374", Swamp::new),
            new CardPrinting("10E", "375", Swamp::new)
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
