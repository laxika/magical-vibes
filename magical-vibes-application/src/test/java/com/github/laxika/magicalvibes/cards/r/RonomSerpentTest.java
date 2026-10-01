package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cryoclasm;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RonomSerpent.class, SnowCoveredIsland.class, Cryoclasm.class})
class RonomSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no snow lands")
    void sacrificedWhenNoSnowLands() {
        harness.setHand(player1, List.of(new RonomSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ronom Serpent");
        harness.assertInGraveyard(player1, "Ronom Serpent");
    }

    @Test
    @DisplayName("Survives while controller controls a snow land")
    void survivesWithSnowLand() {
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.setHand(player1, List.of(new RonomSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ronom Serpent");
    }

    @Test
    @DisplayName("Is sacrificed after its last snow land leaves the battlefield")
    void sacrificedAfterLastSnowLandLeaves() {
        var snowLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        harness.setHand(player1, List.of(new RonomSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ronom Serpent");

        harness.setHand(player2, List.of(new Cryoclasm()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, snowLand.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ronom Serpent");
        harness.assertInGraveyard(player1, "Ronom Serpent");
    }

    @Test
    @CardUsed(Island.class)
    @DisplayName("A nonsnow land does not satisfy the state trigger")
    void nonsnowLandDoesNotSatisfyStateTrigger() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new RonomSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ronom Serpent");
        harness.assertInGraveyard(player1, "Ronom Serpent");
    }

    @Test
    @DisplayName("Can attack when defending player controls a snow land")
    void canAttackWhenDefenderControlsSnowLand() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.addToBattlefield(player2, new SnowCoveredIsland());

        addCreatureReady(player1, new RonomSerpent());

        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no snow land")
    void cannotAttackWhenDefenderHasNoSnowLand() {
        harness.addToBattlefield(player1, new SnowCoveredIsland());

        addCreatureReady(player1, new RonomSerpent());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
