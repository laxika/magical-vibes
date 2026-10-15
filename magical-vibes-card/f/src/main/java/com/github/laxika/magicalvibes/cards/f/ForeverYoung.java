package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ELD", collectorNumber = "89")
public class ForeverYoung extends Card {

    public ForeverYoung() {
        addEffect(EffectSlot.SPELL, new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(
                new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
