package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.cards.a.AncestorsChosen;
import com.github.laxika.magicalvibes.cards.a.AngelicBlessing;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
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
            new CardPrinting("10E", "1", AncestorsChosen::new),
            new CardPrinting("10E", "2", AngelOfMercy::new, "\"Every tear shed is a drop of immortality.\""),
            new CardPrinting("10E", "3", AngelicBlessing::new, "\"Only the warrior who can admit mortal weakness will be bolstered by immortal strength.\""),
            new CardPrinting("10E", "4", AngelicChorus::new, "\"The harmony of the glorious is a dirge to the wicked.\""),
            new CardPrinting("10E", "5", AngelicWall::new, "\"The Ancestor protects us in ways we can't begin to comprehend.\"\n\u2014Mystic elder"),
            new CardPrinting("10E", "6", AuraOfSilence::new, "Not all silences are easily broken."),
            new CardPrinting("10E", "7", AvenCloudchaser::new),
            new CardPrinting("10E", "380", Forest::new),
            new CardPrinting("10E", "381", Forest::new),
            new CardPrinting("10E", "382", Forest::new),
            new CardPrinting("10E", "383", Forest::new),
            new CardPrinting("10E", "267", GiantSpider::new, "\"When I trained under Multani, he bade me sit for hours watching Yavimaya's spiders. From them I learned stealth, patience, and the best way to snare a wind drake.\"\n\u2014Mirri of the Weatherlight"),
            new CardPrinting("10E", "268", GrizzlyBears::new, "\"We cannot forget that among all of Dominaria's wonders, a system of life exists, with prey and predators that will never fight wars nor vie for ancient power.\"\n\u2014Jolrael, empress of beasts"),
            new CardPrinting("10E", "269", HuntedWumpus::new, "\"Just one can feed a dozen people for a month.\""),
            new CardPrinting("10E", "270", Hurricane::new, "\"Don't envy the grandeur of drakes. Their impression of grace fades when the first gust sends them crashing into one another.\"\n\u2014Molimo, maro-sorcerer"),
            new CardPrinting("10E", "274", LlanowarElves::new, "\"One bone broken for every twig snapped underfoot.\"\n\u2014Llanowar penalty for trespassing"),
            new CardPrinting("10E", "277", MightOfOaks::new, "\"Guess where I'm gonna plant this!\""),
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
