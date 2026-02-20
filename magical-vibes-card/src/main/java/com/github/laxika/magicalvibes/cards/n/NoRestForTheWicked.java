package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureCardsPutIntoYourGraveyardFromBattlefieldThisTurnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "165")
public class NoRestForTheWicked extends Card {

    public NoRestForTheWicked() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new ReturnCreatureCardsPutIntoYourGraveyardFromBattlefieldThisTurnToHandEffect()
                ),
                false,
                "Sacrifice No Rest for the Wicked: Return to your hand all creature cards put into your graveyard from the battlefield this turn."
        ));
    }
}
