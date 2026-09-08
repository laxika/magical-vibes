package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "14")
@CardRegistration(set = "DRK", collectorNumber = "19")
@CardRegistration(set = "TSB", collectorNumber = "17")
public class WitchHunter extends Card {

    public WitchHunter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "{T}: This creature deals 1 damage to target player or planeswalker."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}{W}",
                List.of(ReturnToHandEffect.target()),
                "{1}{W}{W}, {T}: Return target creature an opponent controls to its owner's hand.",
                TargetFilters.creatureAnOpponentControls()
        ));
    }
}
