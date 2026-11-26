package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ELD", collectorNumber = "71")
@CardRegistration(set = "ELD", collectorNumber = "349")
public class VantressGargoyle extends Card {

    public VantressGargoyle() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new OpponentGraveyardAtLeast(7),
                "defending player has seven or more cards in their graveyard"));
        addStaticEffect(CantAttackOrBlockUnlessEffect.block(
                new CardsInHandAtLeast(4),
                "you have four or more cards in hand"));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new MillEffect(1, Subjects.eachPlayer())),
                "{T}: Each player mills a card."));
    }
}
