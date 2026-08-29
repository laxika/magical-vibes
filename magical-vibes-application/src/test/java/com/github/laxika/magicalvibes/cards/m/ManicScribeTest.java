package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManicScribeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and makes each opponent mill three cards")
    void millsEachOpponentOnEnter() {
        harness.setLibrary(player1, library(5));
        harness.setLibrary(player2, library(5));
        harness.setHand(player1, List.of(new ManicScribe()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Delirium mills the opponent whose upkeep it is")
    void millsOpponentOnUpkeepWithDelirium() {
        harness.setLibrary(player2, library(5));
        setDelirium();
        harness.addToBattlefield(player1, new ManicScribe());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not mill on an opponent's upkeep without delirium")
    void doesNotMillWithoutDelirium() {
        harness.setLibrary(player2, library(5));
        harness.addToBattlefield(player1, new ManicScribe());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Does not trigger on its controller's upkeep")
    void doesNotTriggerOnOwnUpkeep() {
        harness.setLibrary(player2, library(5));
        setDelirium();
        harness.addToBattlefield(player1, new ManicScribe());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Rechecks delirium when the triggered ability resolves")
    void rechecksDeliriumAtResolution() {
        harness.setLibrary(player2, library(5));
        setDelirium();
        harness.addToBattlefield(player1, new ManicScribe());

        advanceToUpkeep(player2);
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }

    private List<Card> library(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Forest());
        }
        return cards;
    }
}
