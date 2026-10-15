package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "116")
@CardRegistration(set = "PIP", collectorNumber = "426")
@CardRegistration(set = "PIP", collectorNumber = "644")
@CardRegistration(set = "PIP", collectorNumber = "954")
public class RedDeathShipwrecker extends Card {

    public RedDeathShipwrecker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new GoadTargetCreatureUntilNextTurnEffect(),
                        new TargetPermanentControllerDrawsCardEffect(),
                        new AwardManaEffect(ManaColor.RED)),
                "{T}: Goad target creature an opponent controls. That player draws a card. You add {R}.",
                TargetFilters.creatureAnOpponentControls()));
    }
}
