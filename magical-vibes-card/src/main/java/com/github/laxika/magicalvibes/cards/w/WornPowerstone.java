package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "318")
@CardRegistration(set = "DDE", collectorNumber = "24")
@CardRegistration(set = "EMA", collectorNumber = "235")
@CardRegistration(set = "HA7", collectorNumber = "18")
@CardRegistration(set = "C15", collectorNumber = "275")
public class WornPowerstone extends Card {

    public WornPowerstone() {
        // This artifact enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {C}{C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 2)),
                "{T}: Add {C}{C}."
        ));
    }
}
