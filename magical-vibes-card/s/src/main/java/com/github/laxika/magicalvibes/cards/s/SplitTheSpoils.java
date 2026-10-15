package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardsAndSeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "HBG", collectorNumber = "223")
public class SplitTheSpoils extends Card {

    public SplitTheSpoils() {
        addEffect(EffectSlot.SPELL, new ExileTargetGraveyardCardsAndSeparateIntoPilesEffect(
                new CardIsPermanentPredicate(), 5, GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                CardPileDisposition.HAND, true));
    }
}
