package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostEquippedCreatureIfNameSharedEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "23")
public class MaceOfDisruption extends Card {

    public MaceOfDisruption() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new ProtectionFromSubtypesEffect(Set.of(CardSubtype.DEMON, CardSubtype.DEVIL)),
                GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ATTACK, new PerpetuallyBoostEquippedCreatureIfNameSharedEffect(1, 0));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
