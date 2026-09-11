package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerOwnsCardInExile;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "57")
public class DreadlightMonstrosity extends Card {

    public DreadlightMonstrosity() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}{U}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{3}{U}{U}: This creature can't be blocked this turn. Activate only if you own a card in exile."
        ).withActivationCondition(
                new ControllerOwnsCardInExile(),
                "You don't own a card in exile"
        ));
    }
}
