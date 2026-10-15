package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ActivateCreatureAbilitiesAsThoughHasteEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "187")
public class ShangChiMasterOfKungFu extends Card {

    public ShangChiMasterOfKungFu() {
        addEffect(EffectSlot.STATIC, new ActivateCreatureAbilitiesAsThoughHasteEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2, ManaSpendRestriction.CREATURE_ABILITIES)),
                "{T}: Add two mana of any one color. Spend this mana only to activate abilities of creature sources."
        ));
    }
}
