package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "326")
@CardRegistration(set = "V12", collectorNumber = "12")
@CardRegistration(set = "DSC", collectorNumber = "297")
public class ShivanGorge extends Card {

    public ShivanGorge() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)),
                "{2}{R}, {T}: Shivan Gorge deals 1 damage to each opponent."
        ));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
