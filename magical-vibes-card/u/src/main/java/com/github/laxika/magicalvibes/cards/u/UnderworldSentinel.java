package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "293")
public class UnderworldSentinel extends Card {

    public UnderworldSentinel() {
        addTrigger(Triggers.attacks(),
                new ExileTargetCardFromGraveyardAndTrackWithSourceEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnAllCardsExiledWithSourceEffect(true));
    }
}
