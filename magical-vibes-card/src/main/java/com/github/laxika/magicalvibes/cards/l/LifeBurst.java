package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "ODY", collectorNumber = "30")
public class LifeBurst extends Card {

    public LifeBurst() {
        // Target player gains 4 life, then gains 4 life for each card named Life Burst in each graveyard.
        addEffect(EffectSlot.SPELL, new TargetPlayerGainsLifeEffect(4));
        addEffect(EffectSlot.SPELL, new TargetPlayerGainsLifeEffect(new Scaled(
                        new CardsInGraveyard(new CardNamedPredicate("Life Burst"), CountScope.ANY_PLAYER), 4)));
    }
}
