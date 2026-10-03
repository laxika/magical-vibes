package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BlizzardStrix.class, GrizzlyBears.class, SnowCoveredIsland.class})
class BlizzardStrixTest extends BaseCardTest {

    @Test
    @DisplayName("With another snow permanent, ETB exiles a target permanent until the next end step")
    void flickersTargetWithAnotherSnowPermanent() {
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlizzardStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        var targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without another snow permanent, the ETB ability does not trigger")
    void doesNotFlickerWithoutAnotherSnowPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlizzardStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's snow permanent does not satisfy the condition")
    void opponentsSnowPermanentDoesNotEnableTrigger() {
        harness.addToBattlefield(player2, new SnowCoveredIsland());
        harness.setHand(player1, List.of(new BlizzardStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Snow-Covered Island");
    }

    @Test
    @DisplayName("The ability does nothing if the other snow permanent leaves before resolution")
    void rechecksSnowConditionOnResolution() {
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.addToBattlefield(player2, new SnowCoveredIsland());
        harness.setHand(player1, List.of(new BlizzardStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        var targetId = harness.getPermanentId(player2, "Snow-Covered Island");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof SnowCoveredIsland);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Snow-Covered Island");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The only other snow permanent can itself be exiled and returned")
    void canTargetItsOwnSnowLand() {
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.setHand(player1, List.of(new BlizzardStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        var targetId = harness.getPermanentId(player1, "Snow-Covered Island");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Snow-Covered Island");
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Snow-Covered Island");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Snow-Covered Island");
        assertThat(harness.getPermanentId(player1, "Snow-Covered Island")).isNotEqualTo(targetId);
    }

    @Test
    @DisplayName("A stolen permanent returns to its owner even if Blizzard Strix leaves")
    void returnsToOwnerAfterSourceLeaves() {
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.addToBattlefield(player2, new SnowCoveredIsland());
        var targetId = harness.getPermanentId(player2, "Snow-Covered Island");
        gd.stolenCreatures.put(targetId, player1.getId());
        harness.setHand(player1, List.of(new BlizzardStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof BlizzardStrix);

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snow-Covered Island");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof SnowCoveredIsland)
                .hasSize(2);
    }

    @Test
    @DisplayName("The entering Blizzard Strix cannot target itself")
    void cannotTargetItself() {
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.setHand(player1, List.of(new BlizzardStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        var sourceId = harness.getPermanentId(player1, "Blizzard Strix");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, sourceId))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Snow-Covered Island"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blizzard Strix");
        harness.assertNotOnBattlefield(player1, "Snow-Covered Island");
    }
}
