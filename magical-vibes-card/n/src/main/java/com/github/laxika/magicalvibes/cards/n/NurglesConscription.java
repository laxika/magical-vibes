package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardOfTargetCardOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "40K", collectorNumber = "44")
public class NurglesConscription extends Card {

    public NurglesConscription() {
        addSpellEffect(new PutCardFromOpponentGraveyardOntoBattlefieldEffect(
                        true, new CardTypePredicate(CardType.CREATURE), false));
        addSpellEffect(new ExileGraveyardOfTargetCardOwnerEffect());
    }
}
