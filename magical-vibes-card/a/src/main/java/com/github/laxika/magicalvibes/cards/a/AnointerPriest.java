package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;


@CardRegistration(set = "AKH", collectorNumber = "3")
@CardRegistration(set = "AKR", collectorNumber = "3")
public class AnointerPriest extends Card {

    public AnointerPriest() {
        // Whenever a creature token you control enters, you gain 1 life.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentIsTokenPredicate())),
                        ObjectRelation.YOU_CONTROL),
                new GainLifeEffect(1));

        // Embalm {3}{W} ({3}{W}, Exile this card from your graveyard: Create a token that's a copy
        // of it, except it's a white Zombie Human Cleric with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{3}{W}", "Human Cleric");
    }
}
