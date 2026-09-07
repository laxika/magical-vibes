package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "8")
public class DancingSword extends Card {

    public DancingSword() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 1, GrantScope.EQUIPPED_CREATURE));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceIsCreature(),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                        new CounterUnlessPaysEffect(1),
                        GrantScope.SELF)));

        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new MayEffect(
                new BecomeCreatureEffect(2, 1, List.of(CardSubtype.CONSTRUCT),
                        Set.of(Keyword.FLYING), Set.of(), Set.of(CardType.ARTIFACT)),
                "Have Dancing Sword become a creature?"));

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
