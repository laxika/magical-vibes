package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MRD", collectorNumber = "181")
public class GolemSkinGauntlets extends Card {

    public GolemSkinGauntlets() {
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new AttachmentsOnSource(false, true), new Fixed(0), GrantScope.EQUIPPED_CREATURE, true));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
