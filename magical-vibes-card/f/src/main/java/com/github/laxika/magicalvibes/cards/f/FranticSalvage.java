package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MBS", collectorNumber = "6")
public class FranticSalvage extends Card {

    public FranticSalvage() {
        addEffect(EffectSlot.SPELL, new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(new CardTypePredicate(CardType.ARTIFACT)));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
