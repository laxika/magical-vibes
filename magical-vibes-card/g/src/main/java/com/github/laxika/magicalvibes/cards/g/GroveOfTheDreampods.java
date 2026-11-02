package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "18")
@CardRegistration(set = "OPCA", collectorNumber = "34")
@CardRegistration(set = "MOC", collectorNumber = "145")
public class GroveOfTheDreampods extends Card {

    public GroveOfTheDreampods() {
        RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect revealCreature =
                new RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect(
                        new Fixed(1), new CardTypePredicate(CardType.CREATURE), false, true);
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, revealCreature);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), revealCreature);

        addEffect(EffectSlot.CHAOS_TRIGGERED, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build());
    }
}
