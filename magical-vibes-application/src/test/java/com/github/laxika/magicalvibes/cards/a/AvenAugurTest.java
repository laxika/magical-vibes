package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenAugur.class, GrizzlyBears.class})
class AvenAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it during upkeep returns two target creatures")
    void sacrificeDuringUpkeepReturnsTwoCreatures() {
        addCreatureReady(player1, new AvenAugur());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        prepareForUpkeep(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Augur");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("The ability can return one or no target creatures")
    void abilityCanReturnFewerThanTwoCreatures() {
        addCreatureReady(player1, new AvenAugur());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareForUpkeep(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Aven Augur");
    }

    @Test
    @DisplayName("The ability may sacrifice Aven Augur without choosing targets")
    void abilityMayChooseNoTargets() {
        addCreatureReady(player1, new AvenAugur());
        prepareForUpkeep(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Augur");
    }

    @Test
    @DisplayName("The ability can be activated only during its controller's upkeep")
    void abilityRequiresYourUpkeep() {
        addCreatureReady(player1, new AvenAugur());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void abilityCannotTargetNoncreature() {
        addCreatureReady(player1, new AvenAugur());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Island());
        prepareForUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareForUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }
}
