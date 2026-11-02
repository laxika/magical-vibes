package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "91")
@CardRegistration(set = "SOS", collectorNumber = "326")
public class MoseoVeinsNewDean extends Card {

    public MoseoVeinsNewDean() {
        // Flying is auto-loaded from Scryfall.

        // When Moseo enters, create a 1/1 black and green Pest creature token with
        // "Whenever this token attacks, you gain 1 life."
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                CardType.CREATURE, 1, "Pest", 1, 1,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.PEST), Set.of(), Set.of(),
                false, false,
                List.of(LegacySlots.triggered(EffectSlot.ON_ATTACK, new GainLifeEffect(1))),
                List.of(), false, false, false, 0, Set.of()));

        // Infusion — At the beginning of your end step, if you gained life this turn, return up to
        // one target creature card with mana value X or less from your graveyard to the battlefield,
        // where X is the amount of life you gained this turn.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new GainedLifeThisTurn(),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .upTo(true)
                        .maxManaValueEqualsLifeGainedThisTurn(true)
                        .build()));
    }
}
