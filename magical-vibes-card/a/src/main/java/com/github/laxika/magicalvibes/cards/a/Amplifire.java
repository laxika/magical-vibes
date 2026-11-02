package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateSetSelfBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "92")
public class Amplifire extends Card {

    public Amplifire() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RevealUntilCardPredicateSetSelfBasePowerToughnessEffect(
                        new CardTypePredicate(CardType.CREATURE), 2));
    }
}
