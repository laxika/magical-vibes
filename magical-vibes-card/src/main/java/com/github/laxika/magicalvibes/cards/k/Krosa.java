package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "OHOP", collectorNumber = "20")
public class Krosa extends Card {

    public Krosa() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ALL_CREATURES));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new AwardManaEffect(ManaColor.WHITE),
                        new AwardManaEffect(ManaColor.BLUE),
                        new AwardManaEffect(ManaColor.BLACK),
                        new AwardManaEffect(ManaColor.RED),
                        new AwardManaEffect(ManaColor.GREEN)),
                "Add {W}{U}{B}{R}{G}?"));
    }
}
