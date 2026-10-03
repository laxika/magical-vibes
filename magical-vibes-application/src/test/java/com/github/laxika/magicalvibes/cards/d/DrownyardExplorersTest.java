package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrownyardExplorers.class})
class DrownyardExplorersTest extends BaseCardTest {

    @Test
    @DisplayName("When Drownyard Explorers enters, one Clue token is created")
    void etbCreatesOneClueToken() {
        harness.setHand(player1, List.of(new DrownyardExplorers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }
    @Test
    @DisplayName("Investigate waits for its trigger to resolve and creates a Clue only for its controller")
    void investigateUsesTheStack() {
        harness.setHand(player1, List.of(new DrownyardExplorers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.assertNotOnBattlefield(player1, "Clue");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drownyard Explorers");
        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Clue");
    }

    @Test
    @DisplayName("The Clue is sacrificed immediately for two mana and draws only on resolution")
    void clueSacrificeDrawsOnResolution() {
        harness.setHand(player1, List.of(new DrownyardExplorers()));
        harness.setLibrary(player1, List.of(new DrownyardExplorers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Drownyard Explorers");
        harness.assertOnBattlefield(player1, "Drownyard Explorers");
    }
}
