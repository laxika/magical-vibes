package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongTriggeringCards;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "23")
public class PollutedCisternDimOubliette extends Card {

    public PollutedCisternDimOubliette() {
        setRoomDoorManaCosts(List.of("{1}{B}", "{4}{B}"));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Polluted Cistern", List.of())
                        .withManaCost("{1}{B}"),
                new ChooseOneEffect.ChooseOneOption("Dim Oubliette", List.of())
                        .withManaCost("{4}{B}")
        )));

        addStaticEffect(new ConditionalEffect(
                new SourceRoomDoorUnlocked(0),
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN,
                                ObjectRelation.YOU_OWN),
                                TriggerOptions.ONE_OR_MORE, List.of(new EffectRegistration(
                                        new LoseLifeEffect(new CardTypesAmongTriggeringCards(),
                                                LoseLifeRecipient.EACH_OPPONENT)))),
                        GrantScope.SELF)));
        addTrigger(Triggers.doorUnlocked(1),
                SequenceEffect.of(
                        new MillEffect(3, MillRecipient.CONTROLLER),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .mandatory(true)
                                .build()));
    }
}
