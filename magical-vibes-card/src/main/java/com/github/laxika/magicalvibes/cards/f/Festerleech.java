package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "85")
public class Festerleech extends Card {

    public Festerleech() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MillEffect(2, MillRecipient.TARGET_PLAYER));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new BoostSelfEffect(2, 2)),
                "{1}{B}: This creature gets +2/+2 until end of turn. Activate only once each turn.",
                1));
    }
}
