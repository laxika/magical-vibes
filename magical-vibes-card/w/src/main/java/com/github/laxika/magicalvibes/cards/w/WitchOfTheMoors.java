package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1722")
@CardRegistration(set = "SOC", collectorNumber = "230")
@CardRegistration(set = "OTC", collectorNumber = "152")
public class WitchOfTheMoors extends Card {

    public WitchOfTheMoors() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new GainedLifeThisTurn()),
                SequenceEffect.of(
                        new SacrificePermanentsEffect(
                                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .upTo(true)
                                .build()));
    }
}
