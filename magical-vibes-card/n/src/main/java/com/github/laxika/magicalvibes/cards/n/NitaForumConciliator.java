package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "206")
@CardRegistration(set = "SOS", collectorNumber = "354")
public class NitaForumConciliator extends Card {

    public NitaForumConciliator() {
        // Whenever you cast a spell you don't own, put a +1/+1 counter on each creature you control.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardControllerDoesNotOwnPredicate(),
                List.of(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()))
        ));

        // {2}, Sacrifice another creature: Exile target instant or sorcery card from an opponent's
        // graveyard. You may cast it this turn, and mana of any type can be spent to cast that spell.
        // If that spell would be put into a graveyard, exile it instead. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect()
                ),
                "{2}, Sacrifice another creature: Exile target instant or sorcery card from an "
                        + "opponent's graveyard. You may cast it this turn, and mana of any type can be "
                        + "spent to cast that spell. If that spell would be put into a graveyard, exile it "
                        + "instead. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
