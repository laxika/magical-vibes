package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LordOfTheUlvenwald;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "231")
public class KessigNaturalist extends Card {

    public KessigNaturalist() {
        setBackFaceCard(new LordOfTheUlvenwald());
        addEffect(EffectSlot.ON_ATTACK, addManaEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "LordOfTheUlvenwald";
    }

    private static ChooseOneEffect addManaEffect() {
        return new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Add {R}",
                        new AwardPersistentManaEffect(ManaColor.RED, new Fixed(1))),
                new ChooseOneEffect.ChooseOneOption("Add {G}",
                        new AwardPersistentManaEffect(ManaColor.GREEN, new Fixed(1)))
        ));
    }
}
