package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "181")
public class GreenhouseRicketyGazebo extends Card {

    public GreenhouseRicketyGazebo() {
        setRoomDoorManaCosts(List.of("{2}{G}", "{3}{G}"));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Greenhouse", List.of())
                        .withManaCost("{2}{G}"),
                new ChooseOneEffect.ChooseOneOption("Rickety Gazebo", List.of())
                        .withManaCost("{3}{G}")
        )));

        addStaticEffect(new ConditionalEffect(
                new SourceRoomDoorUnlocked(0),
                new GrantActivatedAbilityEffect(
                        ManaAbilities.tapForAnyColor(), Subjects.landsYouControl())));

        addTrigger(Triggers.doorUnlocked(1), SequenceEffect.of(
                new MillEffect(4, Subjects.you()),
                ChooseCardEffect.upTo(Subjects.cardsThisWay(StepKind.MILL, new CardIsPermanentPredicate()), 2,
                        "permanent cards milled this way"),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));
    }
}
