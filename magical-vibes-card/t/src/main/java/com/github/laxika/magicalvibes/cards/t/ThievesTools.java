package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "AFR", collectorNumber = "122")
@CardRegistration(set = "HBG", collectorNumber = "171")
public class ThievesTools extends Card {

    public ThievesTools() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentPowerAtMostPredicate(3), new CantBeBlockedEffect(), null));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
