package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "101")
@CardRegistration(set = "TMT", collectorNumber = "199")
public class RaphaelMostAttitude extends Card {

    public RaphaelMostAttitude() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL), new MayEffect(
                new ExileTopCardsToSourceEffect(1, false),
                "Exile the top card of your library?"));

        addTrigger(Triggers.attacks(),
                new AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(null, false));
    }
}
