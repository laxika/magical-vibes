package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "2X2", collectorNumber = "164")
public class WebweaverChangeling extends Card {

    public WebweaverChangeling() {
        // When this creature enters, if there are three or more creature cards in your graveyard,
        // you gain 5 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new GraveyardCardThreshold(3, new CardTypePredicate(CardType.CREATURE)),
                new GainLifeEffect(5)));
    }
}
