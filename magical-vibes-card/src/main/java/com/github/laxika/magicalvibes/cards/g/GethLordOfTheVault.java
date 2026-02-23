package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "64")
public class GethLordOfTheVault extends Card {

    public GethLordOfTheVault() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{B}",
                List.of(new PutCardFromOpponentGraveyardOntoBattlefieldEffect(true)),
                "{X}{B}: Put target artifact or creature card with mana value X from an opponent's graveyard onto the battlefield under your control tapped. Then that player mills X cards."
        ));
    }
}
