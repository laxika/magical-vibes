package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.ChooseBlockersThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreaturesToBlockThisTurnIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.RollTwoD20IgnoreLowerEffect;

@CardRegistration(set = "AFC", collectorNumber = "29")
public class BerserkersFrenzy extends Card {

    public BerserkersFrenzy() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.BEFORE_BLOCKERS_DECLARED);
        addEffect(EffectSlot.SPELL, new RollTwoD20IgnoreLowerEffect(
                new ChooseCreaturesToBlockThisTurnIfAbleEffect(),
                new ChooseBlockersThisCombatEffect(true),
                14));
    }
}
