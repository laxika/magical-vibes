package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;


@CardRegistration(set = "AKH", collectorNumber = "3")
public class AnointerPriest extends Card {

    public AnointerPriest() {
        // Whenever a creature token you control enters, you gain 1 life.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardIsTokenPredicate(),
                        new GainLifeEffect(1)));

        // Embalm {3}{W} ({3}{W}, Exile this card from your graveyard: Create a token that's a copy
        // of it, except it's a white Zombie Human Cleric with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{3}{W}", "Human Cleric");
    }
}
