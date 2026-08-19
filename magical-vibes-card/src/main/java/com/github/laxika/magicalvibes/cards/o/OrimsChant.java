package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantCastSpellTypesThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.EnumSet;

@CardRegistration(set = "PLS", collectorNumber = "11")
public class OrimsChant extends Card {

    public OrimsChant() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ));
        addEffect(EffectSlot.STATIC, new KickerEffect("{W}"));
        addEffect(EffectSlot.SPELL, new TargetPlayerCantCastSpellTypesThisTurnEffect(
                EnumSet.allOf(CardType.class)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new Kicked(), new CreaturesCantAttackThisTurnEffect()));
    }
}
