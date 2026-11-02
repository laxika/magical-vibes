package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "32")
public class OrimsPrayer extends Card {

    public OrimsPrayer() {
        // Whenever one or more creatures attack you, you gain 1 life for each attacking creature.
        addTrigger(Triggers.creaturesAttackYou(), new GainLifeEffect(
                new PermanentCount(new PermanentIsAttackingSourceControllerPredicate(), CountScope.ANY_PLAYER)));
    }
}
