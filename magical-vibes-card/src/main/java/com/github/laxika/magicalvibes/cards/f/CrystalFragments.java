package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SummonAlexander;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "13")
public class CrystalFragments extends Card {

    public CrystalFragments() {
        setBackFaceCard(new SummonAlexander());

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W}{W}",
                List.of(new ExileSelfAndReturnTransformedEffect()),
                "{5}{W}{W}: Exile this Equipment, then return it to the battlefield transformed under its owner's control.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SummonAlexander";
    }
}
