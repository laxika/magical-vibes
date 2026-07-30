package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScrollOfAvacynTest extends BaseCardTest {

    @Test
    @DisplayName("Without an Angel, only draws a card")
    void drawsWithoutAngel() {
        harness.addToBattlefield(player1, new ScrollOfAvacyn());
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scroll of Avacyn");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("With an Angel, draws a card and gains 5 life")
    void drawsAndGainsLifeWithAngel() {
        harness.addToBattlefield(player1, new ScrollOfAvacyn());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    @DisplayName("An opponent's Angel does not grant the life gain")
    void opponentAngelDoesNotCount() {
        harness.addToBattlefield(player1, new ScrollOfAvacyn());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
