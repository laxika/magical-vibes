package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "11")
@CardRegistration(set = "LTC", collectorNumber = "95")
public class FieldTestedFryingPan extends Card {

    public FieldTestedFryingPan() {
        // When this Equipment enters, create a Food token, then create a 1/1 Halfling token and
        // attach this Equipment to it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                CreateTokenEffect.ofFoodToken(1),
                new LivingWeaponEffect(new CreateTokenEffect(
                        "Halfling", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HALFLING), Set.of(), Set.of()))));

        // Equipped creature has "Whenever you gain life, this creature gets +X/+X until end of
        // turn, where X is the amount of life you gained."
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new BoostSelfEffect(new EventValue(), new EventValue()),
                GrantScope.EQUIPPED_CREATURE));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
