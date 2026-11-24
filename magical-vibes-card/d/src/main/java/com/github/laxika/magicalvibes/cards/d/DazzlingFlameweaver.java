package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBLB", collectorNumber = "23")
public class DazzlingFlameweaver extends Card {

    private static final List<SpellbookCard> SPELLBOOK = List.of(
            new SpellbookCard("RVR", "67"),
            new SpellbookCard("NCC", "34"),
            new SpellbookCard("RNA", "71"),
            new SpellbookCard("RNA", "73"),
            new SpellbookCard("RNA", "181"),
            new SpellbookCard("RVR", "117"),
            new SpellbookCard("RNA", "196"),
            new SpellbookCard("RNA", "109"),
            new SpellbookCard("OTP", "26"),
            new SpellbookCard("RNA", "85"),
            new SpellbookCard("RNA", "118"));

    public DazzlingFlameweaver() {
        // Ward—Pay 3 life.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysLifeEffect(new Fixed(3)));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsCreaturePredicate()),
                TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of(
                        ConjureCardsEffect.atRandomFrom(SPELLBOOK, ConjureDestination.intoExile()),
                        AllowPlayCardsFromExileEffect.untilEndOfYourNextTurn(
                                Subjects.cardsThisWay(StepKind.CONJURE))));
    }
}
