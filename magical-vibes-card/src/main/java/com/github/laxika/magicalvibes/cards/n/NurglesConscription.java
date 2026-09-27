package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardOfTargetCardOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "40K", collectorNumber = "44")
public class NurglesConscription extends Card {

    public NurglesConscription() {
        addEffect(EffectSlot.SPELL,
                new PutCardFromOpponentGraveyardOntoBattlefieldEffect(
                        true, new CardTypePredicate(CardType.CREATURE), false));
        addEffect(EffectSlot.SPELL, new ExileGraveyardOfTargetCardOwnerEffect());
    }
}
