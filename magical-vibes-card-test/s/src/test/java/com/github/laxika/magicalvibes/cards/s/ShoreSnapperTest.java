package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShoreSnapper.class, Island.class})
class ShoreSnapperTest extends BaseCardTest {

    @Test
    @DisplayName("Activating islandwalk ability puts it on the stack for its source")
    void activatingPutsOnStack() {
        Permanent snapper = addCreatureReady(player1, new ShoreSnapper());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(snapper.getId());
    }

    @Test
    @DisplayName("Resolving grants islandwalk until end of turn")
    void resolvingGrantsIslandwalk() {
        Permanent snapper = addCreatureReady(player1, new ShoreSnapper());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, snapper, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Islandwalk granted by ability resets at end of turn cleanup")
    void islandwalkResetsAtEndOfTurn() {
        Permanent snapper = addCreatureReady(player1, new ShoreSnapper());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.ISLANDWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without blue mana")
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new ShoreSnapper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped summoning-sick Snapper can activate and grants islandwalk only to itself")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new ShoreSnapper());
        snapper.setSummoningSick(true);
        snapper.tap();
        Permanent other = addCreatureReady(player1, new ShoreSnapper());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.ISLANDWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.ISLANDWALK)).isFalse();
        assertThat(snapper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted islandwalk prevents blocking when the defender controls an Island")
    void islandwalkPreventsBlockingWithDefendingIsland() {
        Permanent snapper = addCreatureReady(player1, new ShoreSnapper());
        addCreatureReady(player2, new ShoreSnapper());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        snapper.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The attacker's Island does not prevent blocking with granted islandwalk")
    void islandwalkAllowsBlockingWhenOnlyAttackerControlsIsland() {
        Permanent snapper = addCreatureReady(player1, new ShoreSnapper());
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new ShoreSnapper());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        snapper.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Without activating, Snapper can be blocked even if the defender controls an Island")
    void canBeBlockedBeforeActivating() {
        Permanent snapper = addCreatureReady(player1, new ShoreSnapper());
        Permanent blocker = addCreatureReady(player2, new ShoreSnapper());
        harness.addToBattlefield(player2, new Island());

        snapper.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
