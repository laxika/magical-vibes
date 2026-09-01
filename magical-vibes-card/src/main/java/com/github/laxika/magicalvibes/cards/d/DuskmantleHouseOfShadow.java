package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "277")
public class DuskmantleHouseOfShadow extends Card {

    public DuskmantleHouseOfShadow() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{B}",
                List.of(new MillEffect(1, MillRecipient.TARGET_PLAYER)),
                "{U}{B}, {T}: Target player mills a card."
        ));
    }
}
