package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreCardsInHandThanEachOpponent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "64")
public class DeathOfAThousandStings extends Card {

    public DeathOfAThousandStings() {
        addSpellEffect(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addSpellEffect(new GainLifeEffect(1));
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerHasMoreCardsInHandThanEachOpponent()),
                new MayEffect(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                        "Return Death of a Thousand Stings from your graveyard to your hand?"));
    }
}
