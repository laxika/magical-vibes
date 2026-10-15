package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OwnLandsBecomeChosenTypeUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "70")
public class Terraformer extends Card {

    public Terraformer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new OwnLandsBecomeChosenTypeUntilEndOfTurnEffect()),
                "{1}: Choose a basic land type. Each land you control becomes that type until end of turn."
        ));
    }
}
