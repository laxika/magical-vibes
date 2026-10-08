package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "116")
@CardRegistration(set = "V13", collectorNumber = "16")
@CardRegistration(set = "C15", collectorNumber = "178")
@CardRegistration(set = "AFC", collectorNumber = "153")
@CardRegistration(set = "ARC", collectorNumber = "52")
public class ChameleonColossus extends Card {

    public ChameleonColossus() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));
        // {2}{G}{G}: This creature gets +X/+X until end of turn, where X is its power.
        // SourcePower snapshots the effective power at resolution, so repeated activations double it.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{G}",
                List.of(new BoostSelfEffect(new SourcePower(), new SourcePower())),
                "{2}{G}{G}: This creature gets +X/+X until end of turn, where X is its power."
        ));
    }
}
