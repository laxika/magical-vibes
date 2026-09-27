package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "23")
public class PollutedCisternDimOubliette extends Card {

    public PollutedCisternDimOubliette() {
        setRoomDoorManaCosts(List.of("{1}{B}", "{4}{B}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Polluted Cistern", List.of())
                        .withManaCost("{1}{B}"),
                new ChooseOneEffect.ChooseOneOption("Dim Oubliette", List.of())
                        .withManaCost("{4}{B}")
        )));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(0),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
                        new EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect(),
                        GrantScope.SELF)));
        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(1, SequenceEffect.of(
                        new MillEffect(3, MillRecipient.CONTROLLER),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .build())));
    }
}
