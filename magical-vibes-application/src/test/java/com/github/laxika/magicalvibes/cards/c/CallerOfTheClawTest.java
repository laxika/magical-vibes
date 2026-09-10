package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallerOfTheClaw.class, GrizzlyBears.class, Shock.class})
class CallerOfTheClawTest extends BaseCardTest {

    @Test
    void createsOneBearForEachNontokenCreaturePutIntoOwnGraveyardThisTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new CallerOfTheClaw()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bear")).hasSize(1);
    }

    @Test
    void doesNotCountTokenCreaturesPutIntoOwnGraveyard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent token = addCreatureReady(player1, createTokenCreature());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new CallerOfTheClaw()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, token.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bear")).hasSize(1);
    }

    private Card createTokenCreature() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
