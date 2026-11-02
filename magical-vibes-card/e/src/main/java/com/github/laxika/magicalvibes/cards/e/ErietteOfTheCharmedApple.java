package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedBySourceControllerAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "202")
@CardRegistration(set = "WOE", collectorNumber = "299")
@CardRegistration(set = "SOC", collectorNumber = "307")
public class ErietteOfTheCharmedApple extends Card {

    public ErietteOfTheCharmedApple() {
        addStaticEffect(new CreaturesCantAttackControllerUnlessPredicateEffect(
                new PermanentNotPredicate(new PermanentIsEnchantedBySourceControllerAuraPredicate()),
                true));

        PermanentCount aurasYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.AURA), CountScope.CONTROLLER);
        addTrigger(Triggers.endStep(ActivePlayer.YOU), SequenceEffect.of(
                new LoseLifeEffect(aurasYouControl, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(aurasYouControl)));
    }
}
