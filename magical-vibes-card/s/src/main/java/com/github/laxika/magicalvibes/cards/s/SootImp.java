package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "43")
@CardRegistration(set = "DDC", collectorNumber = "37")
@CardRegistration(set = "DVD", collectorNumber = "37")
public class SootImp extends Card {

    public SootImp() {
        // Flying is auto-loaded as a keyword from Scryfall.

        // Whenever a player casts a nonblack spell, that player loses 1 life.
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardNotPredicate(new CardColorPredicate(CardColor.BLACK))),
                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER));
    }
}
