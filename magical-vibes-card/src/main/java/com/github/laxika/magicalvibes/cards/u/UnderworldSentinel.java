package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "THB", collectorNumber = "293")
public class UnderworldSentinel extends Card {

    public UnderworldSentinel() {
        addEffect(EffectSlot.ON_ATTACK,
                new ExileTargetCardFromGraveyardAndTrackWithSourceEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        addEffect(EffectSlot.ON_DEATH, new ReturnAllCardsExiledWithSourceEffect(true));
    }
}
