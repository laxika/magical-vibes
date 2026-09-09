package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "52")
public class HarmonizedTrioBrainstorm extends Card {

    public HarmonizedTrioBrainstorm() {
        setBackFaceCard(new Brainstorm());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate(), true),
                        new BecomePreparedEffect()),
                "{T}, Tap two untapped creatures you control: This creature becomes prepared."));
    }

    @Override
    public String getBackFaceClassName() {
        return "Brainstorm";
    }
}
