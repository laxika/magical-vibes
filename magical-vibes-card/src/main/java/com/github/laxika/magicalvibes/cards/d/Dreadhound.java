package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "MID", collectorNumber = "97")
@CardRegistration(set = "PIO", collectorNumber = "92")
@CardRegistration(set = "DBL", collectorNumber = "97")
public class Dreadhound extends Card {

    public Dreadhound() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(3, MillRecipient.CONTROLLER));
        // "Whenever a creature dies" includes this creature's own death (it looks back, CR 603.10a).
        LoseLifeEffect loseLife = new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT);
        addEffect(EffectSlot.ON_DEATH, loseLife);
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, loseLife);
        addEffect(EffectSlot.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
