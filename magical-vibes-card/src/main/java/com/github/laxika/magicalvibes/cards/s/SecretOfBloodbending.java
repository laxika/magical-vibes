package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayerNextCombatOrTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "TLA", collectorNumber = "69")
public class SecretOfBloodbending extends Card {

    public SecretOfBloodbending() {
        addEffect(EffectSlot.SPELL, WaterbendCost.optional(10));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addEffect(EffectSlot.SPELL, new ControlTargetPlayerNextCombatOrTurnEffect())
                .addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
