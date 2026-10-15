package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.t.Thunderbolt;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScrollOfAvacyn.class, SeraphOfDawn.class, Thunderbolt.class})
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
        harness.addToBattlefield(player1, new SeraphOfDawn());
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
        harness.addToBattlefield(player2, new SeraphOfDawn());
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and an Angel entering before resolution counts")
    void angelEnteringBeforeResolutionCounts() {
        harness.addToBattlefield(player1, new ScrollOfAvacyn());
        harness.addMana(player1, ManaColor.WHITE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Scroll of Avacyn");
        harness.assertInGraveyard(player1, "Scroll of Avacyn");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertLife(player1, lifeBefore);

        harness.addToBattlefield(player1, new SeraphOfDawn());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertLife(player1, lifeBefore + 5);
    }

    @Test
    @DisplayName("An Angel destroyed in response does not grant life")
    void angelRemovedBeforeResolutionDoesNotCount() {
        harness.addToBattlefield(player1, new ScrollOfAvacyn());
        harness.addToBattlefield(player1, new SeraphOfDawn());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new Thunderbolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.castModalInstant(player2, 0, 1,
                List.of(harness.getPermanentId(player1, "Seraph of Dawn")));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Seraph of Dawn");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Multiple Angels still grant only 5 life")
    void multipleAngelsGainOnlyFiveLife() {
        harness.addToBattlefield(player1, new ScrollOfAvacyn());
        harness.addToBattlefield(player1, new SeraphOfDawn());
        harness.addToBattlefield(player1, new SeraphOfDawn());
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 5);
    }
}
