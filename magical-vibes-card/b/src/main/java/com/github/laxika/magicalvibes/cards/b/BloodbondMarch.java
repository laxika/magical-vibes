package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "192")
public class BloodbondMarch extends Card {

    public BloodbondMarch() {
        addTrigger(Triggers.playerCastsSpell(),
                new ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect());
    }
}
