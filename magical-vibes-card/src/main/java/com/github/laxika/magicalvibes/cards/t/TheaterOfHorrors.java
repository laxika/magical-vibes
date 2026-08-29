package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "213")
public class TheaterOfHorrors extends Card {

    public TheaterOfHorrors() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ExileTopCardsToSourceEffect(1, false));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentLostLifeThisTurn(1),
                new AllowCastFromCardsExiledWithSourceEffect(false, null, false, true, 0)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1, PlayerRelation.OPPONENT)),
                "{3}{R}: This enchantment deals 1 damage to target opponent or planeswalker.",
                new AnyTargetPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent or planeswalker")));
    }
}
