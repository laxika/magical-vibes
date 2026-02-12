package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.cards.a.AncestorsChosen;
import com.github.laxika.magicalvibes.cards.a.AngelicBlessing;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
import com.github.laxika.magicalvibes.cards.b.BallistaSquad;
import com.github.laxika.magicalvibes.cards.b.Bandage;
import com.github.laxika.magicalvibes.cards.b.BeaconOfImmortality;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.c.Condemn;
import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HailOfArrows;
import com.github.laxika.magicalvibes.cards.h.HeartOfLight;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.h.HuntedWumpus;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.k.KjeldoranRoyalGuard;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.NomadMythmaker;
import com.github.laxika.magicalvibes.cards.l.LoyalSentry;
import com.github.laxika.magicalvibes.cards.l.Luminesce;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.m.Mobilization;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.cards.p.Pariah;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.ReyaDawnbringer;
import com.github.laxika.magicalvibes.cards.r.RevivingDose;
import com.github.laxika.magicalvibes.cards.r.Righteousness;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SkyhunterPatrol;
import com.github.laxika.magicalvibes.cards.s.SkyhunterProwler;
import com.github.laxika.magicalvibes.cards.s.SkyhunterSkirmisher;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.SpiritLink;
import com.github.laxika.magicalvibes.cards.s.SpiritWeaver;
import com.github.laxika.magicalvibes.cards.s.StarlightInvoker;
import com.github.laxika.magicalvibes.cards.s.SerrasEmbrace;
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
            new CardPrinting("10E", "8", BallistaSquad::new, "\"The perfect antidote for a tightly packed formation.\""),
            new CardPrinting("10E", "9", Bandage::new, "\"Life is measured in inches. To a healer, every one of those inches is precious.\""),
            new CardPrinting("10E", "10", BeaconOfImmortality::new, "\"The cave floods with light. A thousand rays shine forth and meld into one.\""),
            new CardPrinting("10E", "11", BenalishKnight::new, "\"We return to the place of our first injustice. We will right the wrongs of our ancestors.\"\n—Barrin, master wizard"),
            new CardPrinting("10E", "12", ChoMannoRevolutionary::new),
            new CardPrinting("10E", "13", Condemn::new),
            new CardPrinting("10E", "14", Demystify::new),
            new CardPrinting("10E", "15", FieldMarshal::new),
            new CardPrinting("10E", "16", GhostWarden::new, "\"I thought of fate as an iron lattice, intricate but rigidly unchangeable. That was until some force bent fate's bars to spare my life.\"\n\u2014Ilromov, traveling storyteller"),
            new CardPrinting("10E", "18", HailOfArrows::new, "\"Do not let a single shaft loose until my word. And when I give that word, do not leave a single shaft in Eiganjo.\"\n\u2014General Takeno"),
            new CardPrinting("10E", "19", HeartOfLight::new, "Harus, the Rune-Reader, pored over the texts: \"In the beginning there was nothing—a profound, luminous nothing rich with potential and power.\""),
            new CardPrinting("10E", "20", HighGround::new),
            new CardPrinting("10E", "21", HolyDay::new, "\"The meek shall inherit the earth, but not today. Today, they merely survive.\"\n\u2014Onean cleric"),
            new CardPrinting("10E", "22", HolyStrength::new),
            new CardPrinting("10E", "23", HonorGuard::new, "\"The strength of one. The courage of ten.\""),
            new CardPrinting("10E", "25", KjeldoranRoyalGuard::new, "\"Upon the frozen tundra stand the Kjeldoran Royal Guard, pikes raised, with the king's oath upon their lips.\""),
            new CardPrinting("10E", "27", LoyalSentry::new, "\"I am not the first to stand in this doorway, and I will not be the last. I stand here because I was told to, and I will not leave until I am told to. This is all that matters.\""),
            new CardPrinting("10E", "28", Luminesce::new, "\"I will not have my flock cower in the shadow of death.\"\n\u2014Lucilde Fiksdotter, Order of the White Shield"),
            new CardPrinting("10E", "29", Mobilization::new),
            new CardPrinting("10E", "30", NomadMythmaker::new, "\"On the wild steppes, history vanishes in the dust. Only the mythmakers remain to say what was, and is, and will be.\""),
            new CardPrinting("10E", "31", Pacifism::new, "For the first time in his life, Grakk felt a little warm and fuzzy inside."),
            new CardPrinting("10E", "32", PaladinEnVec::new),
            new CardPrinting("10E", "33", Pariah::new),
            new CardPrinting("10E", "34", RevivingDose::new, "Samite healers never mix their pungent elixir with sweetener or tea. The threat of a second dose is enough to get most warriors back on their feet."),
            new CardPrinting("10E", "35", ReyaDawnbringer::new, "\"You have not died until I consent.\""),
            new CardPrinting("10E", "36", Righteousness::new, "\"Sometimes the greatest strength is the strength of conviction.\""),
            new CardPrinting("10E", "37", RuleOfLaw::new, "Appointed by the kha himself, members of the tribunal ensure all disputes are settled with the utmost fairness."),
            new CardPrinting("10E", "38", SamiteHealer::new, "\"Healers ultimately acquire the divine gifts of spiritual and physical wholeness. The most devout are also granted the ability to pass physical wholeness on to others.\""),
            new CardPrinting("10E", "39", SerraAngel::new, "\"Her sword sings more beautifully than any choir.\""),
            new CardPrinting("10E", "40", SerrasEmbrace::new),
            new CardPrinting("10E", "41", SkyhunterPatrol::new, "\"We leonin have come to rule the plains by taking to the skies.\"\n\u2014Raksha Golden Cub"),
            new CardPrinting("10E", "42", SkyhunterProwler::new, "\"As tireless as her mount, a skyhunter's vigil is measured in days.\""),
            new CardPrinting("10E", "43", SkyhunterSkirmisher::new),
            new CardPrinting("10E", "44", SoulWarden::new, "\"Count carefully the souls and see that none are lost.\"\n\u2014Vec teaching"),
            new CardPrinting("10E", "45", SpiritLink::new),
            new CardPrinting("10E", "46", SpiritWeaver::new),
            new CardPrinting("10E", "47", StarlightInvoker::new),
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
