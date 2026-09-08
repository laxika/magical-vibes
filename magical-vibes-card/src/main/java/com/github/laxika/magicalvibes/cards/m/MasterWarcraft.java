package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.ChooseAttackersThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBlockersThisCombatEffect;

@CardRegistration(set = "RAV", collectorNumber = "250")
public class MasterWarcraft extends Card {

    public MasterWarcraft() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.BEFORE_ATTACKERS_DECLARED);
        addEffect(EffectSlot.SPELL, new ChooseAttackersThisCombatEffect());
        addEffect(EffectSlot.SPELL, new ChooseBlockersThisCombatEffect());
    }
}
