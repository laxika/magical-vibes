package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutRandomCardExiledWithSourceIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "164")
@CardRegistration(set = "MIC", collectorNumber = "20")
@CardRegistration(set = "MIC", collectorNumber = "58")
public class GorexTheTombshell extends Card {

    public GorexTheTombshell() {
        addCastingCost(new ExileAnyNumberOfCardsFromGraveyardCost(new CardTypePredicate(CardType.CREATURE), 2));
        PutRandomCardExiledWithSourceIntoOwnersHandEffect returnExiledCard =
                new PutRandomCardExiledWithSourceIntoOwnersHandEffect();
        addTrigger(Triggers.attacks(), returnExiledCard);
        addTrigger(Triggers.dies(ObjectRelation.THIS), returnExiledCard);
    }
}
