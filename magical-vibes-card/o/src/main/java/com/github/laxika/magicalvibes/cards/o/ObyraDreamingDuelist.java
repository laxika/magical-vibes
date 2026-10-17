package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "WOE", collectorNumber = "210")
public class ObyraDreamingDuelist extends Card {

    public ObyraDreamingDuelist() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.FAERIE),
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)));
    }
}
