package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "752")
public class SwordsmansSteel extends Card {

    public SwordsmansSteel() {
        PermanentCount equipmentYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(equipmentYouControl));
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                equipmentYouControl, equipmentYouControl, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
