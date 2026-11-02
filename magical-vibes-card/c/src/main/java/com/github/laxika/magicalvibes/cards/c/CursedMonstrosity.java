package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "126")
public class CursedMonstrosity extends Card {

    public CursedMonstrosity() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                new SacrificeUnlessDiscardCardTypeEffect(CardType.LAND));
    }
}
