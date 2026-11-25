package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.stream.Stream;

@CardRegistration(set = "YECL", collectorNumber = "12")
public class LimitlessRekindling extends Card {

    /** Each instant and sorcery card of the ECL expansion, one printing per card. */
    private static final List<SpellbookCard> INSTANTS_AND_SORCERIES = Stream.of(
            "5", "11", "19", "27", "28", "29", "30", "33", "43", "52", "54", "59", "64", "67", "71", "75", "78",
            "79", "80", "83", "84", "85", "89", "91", "94", "97", "100", "113", "115", "116", "119", "123", "127",
            "129", "131", "134", "137", "141", "142", "146", "147", "152", "154", "156", "162", "164", "167",
            "170", "182", "195", "197", "198", "200", "205", "208", "216", "228", "244", "247")
            .map(number -> new SpellbookCard("ECL", number))
            .toList();

    public LimitlessRekindling() {
        addSpellEffect(ConjureCardsEffect.atRandomFrom(INSTANTS_AND_SORCERIES, ConjureDestination.intoExile()));
        addSpellEffect(AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.CONJURE))
                .withoutPayingManaCost());
        addTrigger(Triggers.youCastThisSpell(), CopyTriggeringSpellEffect.storm());
    }
}
