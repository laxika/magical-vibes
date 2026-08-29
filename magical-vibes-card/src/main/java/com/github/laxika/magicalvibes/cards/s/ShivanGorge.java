package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "326")
public class ShivanGorge extends Card {

    public ShivanGorge() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)),
                "{2}{R}, {T}: Shivan Gorge deals 1 damage to each opponent."
        ));
    }
}
