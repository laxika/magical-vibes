package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRB", collectorNumber = "9")
@CardRegistration(set = "CHK", collectorNumber = "122")
@CardRegistration(set = "MMA", collectorNumber = "89")
@CardRegistration(set = "IMA", collectorNumber = "95")
@CardRegistration(set = "SLD", collectorNumber = "1528")
public class KokushoTheEveningStar extends Card {

    public KokushoTheEveningStar() {
        // When Kokusho dies, each opponent loses 5 life. You gain life equal to the life lost this way.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new LoseLifeEffect(5, LoseLifeRecipient.EACH_OPPONENT, true));
    }
}
