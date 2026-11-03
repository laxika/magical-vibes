package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "205")
public class WalkInClosetForgottenCellar extends Card {

    public WalkInClosetForgottenCellar() {
        setRoomDoorManaCosts(List.of("{2}{G}", "{3}{G}{G}"));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Walk-In Closet", List.of())
                        .withManaCost("{2}{G}"),
                new ChooseOneEffect.ChooseOneOption("Forgotten Cellar", List.of())
                        .withManaCost("{3}{G}{G}")
        )));

        addStaticEffect(new ConditionalEffect(
                new SourceRoomDoorUnlocked(0), new PlayLandsFromGraveyardEffect()));
        addTrigger(Triggers.doorUnlocked(1),
                SequenceEffect.of(
                        new AllowCastMatchingCardsFromGraveyardThisTurnEffect(new CardTruePredicate()),
                        new ExileOwnCardsInsteadOfGraveyardUntilEndOfTurnEffect()));
    }
}
