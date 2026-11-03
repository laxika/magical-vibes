package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.OpponentsWhoLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "214")
public class GevScaledScorch extends Card {

    public GevScaledScorch() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysLifeEffect(new Fixed(2)));

        addStaticEffect(ControlledCreaturesEnterWithAdditionalCountersEffect.forAllCreatures(
                        new OpponentsWhoLostLifeThisTurn()));

        target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent")).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardSubtypePredicate(CardSubtype.LIZARD)),
                new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER));
    }
}
