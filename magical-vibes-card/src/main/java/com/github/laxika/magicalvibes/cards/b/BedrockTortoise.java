package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;

@CardRegistration(set = "LCI", collectorNumber = "176")
public class BedrockTortoise extends Card {

    public BedrockTortoise() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.ALL_OWN_CREATURES)));
        addEffect(EffectSlot.STATIC, new AssignCombatDamageWithToughnessEffect(
                GrantScope.ALL_OWN_CREATURES, new PermanentToughnessGreaterThanPowerPredicate()));
    }
}
