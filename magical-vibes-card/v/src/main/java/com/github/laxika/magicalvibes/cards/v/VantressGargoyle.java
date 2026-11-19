package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ELD", collectorNumber = "71")
@CardRegistration(set = "ELD", collectorNumber = "349")
public class VantressGargoyle extends Card {

    public VantressGargoyle() {
        addStaticEffect(new CantAttackUnlessEffect(
                new OpponentGraveyardAtLeast(7),
                "defending player has seven or more cards in their graveyard"));
        addStaticEffect(new CantBlockUnlessEffect(
                new CardsInHandAtLeast(4),
                "you have four or more cards in hand"));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new MillEffect(1, Subjects.you()),
                        new MillEffect(1, Subjects.eachOpponent())),
                "{T}: Each player mills a card."));
    }
}
