package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMID", collectorNumber = "20")
public class OglorDevotedAssistant extends Card {

    public OglorDevotedAssistant() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                LookAtTopCardsEffect.putOneIntoGraveyardRestOnTop(2));
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_OWN),
                new PerpetuallyGrantTriggeredAbilityEffect(
                        CreateTokenEffect.blackZombie(1).withTapped(true)));
    }
}
