package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestToughnessAmongControlled;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "230")
public class HuatliTheSunsHeart extends Card {

    public HuatliTheSunsHeart() {
        addEffect(EffectSlot.STATIC, new AssignCombatDamageWithToughnessEffect(GrantScope.ALL_OWN_CREATURES));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new GainLifeEffect(new GreatestToughnessAmongControlled())),
                "−3: You gain life equal to the greatest toughness among creatures you control."
        ));
    }
}
