package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
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

class MindwrackDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and mills four cards")
    void millsFourCardsOnEnter() {
        harness.setLibrary(player1, library(5));
        harness.setHand(player1, List.of(new MindwrackDemon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Loses four life during upkeep without delirium")
    void losesLifeWithoutDelirium() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not lose life during upkeep with delirium")
    void doesNotLoseLifeWithDelirium() {
        harness.setLife(player1, 20);
        setDelirium();
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Checks delirium when the upkeep ability resolves")
    void checksDeliriumAtResolution() {
        harness.setLife(player1, 20);
        setDelirium();
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not lose life if delirium is gained before resolution")
    void gainsDeliriumBeforeResolution() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        setDelirium();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }

    private List<Card> library(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Naturalize());
        }
        return cards;
    }
}
