package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPG", collectorNumber = "2")
@CardRegistration(set = "LCC", collectorNumber = "161")
public class MalcolmKeenEyedNavigator extends Card {

    public MalcolmKeenEyedNavigator() {
        addTrigger(Triggers.creaturesYouControlDealDamageToOpponent(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.PIRATE),
                        CreateTokenEffect.ofTreasureToken(1)));
    }
}
