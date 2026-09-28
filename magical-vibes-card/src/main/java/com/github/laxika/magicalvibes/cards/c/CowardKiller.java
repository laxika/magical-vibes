package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndCreaturesSharingCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/** Coward // Killer, a split spell with one mode for each half. */
@CardRegistration(set = "WHO", collectorNumber = "77")
public class CowardKiller extends Card {

    public CowardKiller() {
        TargetFilter creature = TargetFilters.creature();
        CardEffect coward = SequenceEffect.of(
                new CantBlockThisTurnEffect(TapUntapScope.TARGET),
                new GrantSubtypeToTargetCreatureEffect(CardSubtype.COWARD, EffectDuration.UNTIL_END_OF_TURN),
                new TimeTravelEffect(1));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Coward — Target creature can't block this turn and becomes a Coward in addition to its other types until end of turn. Time travel.",
                        coward,
                        creature
                ).withManaCost("{1}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Killer — Deals 3 damage to target creature and each other creature that shares a creature type with it",
                        new DealDamageToTargetAndCreaturesSharingCreatureTypeEffect(3),
                        creature
                ).withManaCost("{2}{R}{R}")
        )));
    }
}
