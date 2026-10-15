package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SweepingCleave;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;

@CardRegistration(set = "HBG", collectorNumber = "191")
public class TwoHandedAxe extends Card {

    public TwoHandedAxe() {
        setBackFaceCard(new SweepingCleave());
        addCastingOption(new AdventureCast("{1}{R}"));
        addEffect(EffectSlot.ON_ATTACK,
                new BoostEquippedCreatureUntilEndOfTurnEffect(new SourcePower(), new Fixed(0)));
        addActivatedAbility(new EquipActivatedAbility("{1}{R}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SweepingCleave";
    }
}
