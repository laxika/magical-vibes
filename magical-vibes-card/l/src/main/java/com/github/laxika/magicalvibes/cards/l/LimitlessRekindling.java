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

@CardRegistration(set = "YECL", collectorNumber = "12")
public class LimitlessRekindling extends Card {

    private static final List<SpellbookCard> INSTANTS_AND_SORCERIES = List.of(
            new SpellbookCard("YECL", "3"),
            new SpellbookCard("YECL", "4"),
            new SpellbookCard("YECL", "11"),
            new SpellbookCard("YECL", "12"),
            new SpellbookCard("YECL", "18"),
            new SpellbookCard("YECL", "19"),
            new SpellbookCard("YECL", "33"));

    public LimitlessRekindling() {
        addSpellEffect(ConjureCardsEffect.atRandomFrom(INSTANTS_AND_SORCERIES, ConjureDestination.intoExile()));
        addSpellEffect(AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.CONJURE))
                .withoutPayingManaCost());
        addTrigger(Triggers.youCastThisSpell(), CopyTriggeringSpellEffect.storm());
    }
}
