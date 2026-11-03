package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDSK", collectorNumber = "18")
public class WaryZoneGuard extends Card {

    public WaryZoneGuard() {
        addStaticEffect(new EntersTappedEffect());

        addTrigger(Triggers.secondMainPhase(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()),
                SequenceEffect.of(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.LAND))
                                .targetGraveyard(true)
                                .upTo(true)
                                .build(),
                        new PerpetuallyBoostSourceEffect(1, 1)));
    }
}
