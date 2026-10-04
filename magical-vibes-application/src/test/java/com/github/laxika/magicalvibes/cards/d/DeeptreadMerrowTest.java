package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DeeptreadMerrow.class, Island.class})
class DeeptreadMerrowTest extends BaseCardTest {

    @Test
    @DisplayName("Activating islandwalk ability puts it on the stack")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new DeeptreadMerrow());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving grants islandwalk until end of turn")
    void resolvingGrantsIslandwalk() {
        Permanent merrow = addCreatureReady(player1, new DeeptreadMerrow());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, merrow, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Islandwalk granted by ability resets at end of turn cleanup")
    void islandwalkResetsAtEndOfTurn() {
        Permanent merrow = addCreatureReady(player1, new DeeptreadMerrow());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, merrow, Keyword.ISLANDWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, merrow, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without blue mana")
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new DeeptreadMerrow());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Islandwalk prevents blocking while the defending player controls an Island")
    void islandwalkPreventsBlockingWithIsland() {
        harness.addToBattlefield(player2, new Island());
        Permanent blocker = addCreatureReady(player2, new DeeptreadMerrow());
        Permanent merrow = addCreatureReady(player1, new DeeptreadMerrow());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(merrow);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Islandwalk does not prevent blocking when the defending player controls no Island")
    void islandwalkAllowsBlockingWithoutIsland() {
        Permanent blocker = addCreatureReady(player2, new DeeptreadMerrow());
        Permanent merrow = addCreatureReady(player1, new DeeptreadMerrow());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(merrow);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Islandwalk is gained only when the ability resolves")
    void islandwalkIsNotGrantedBeforeResolution() {
        Permanent merrow = addCreatureReady(player1, new DeeptreadMerrow());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, merrow, Keyword.ISLANDWALK)).isFalse();
        assertThat(merrow.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, merrow, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Merrow can activate its mana-only ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent merrow = harness.addToBattlefieldAndReturn(player1, new DeeptreadMerrow());
        merrow.setSummoningSick(true);
        merrow.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, merrow, Keyword.ISLANDWALK)).isTrue();
        assertThat(merrow.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the Merrow whose ability was activated gains islandwalk")
    void grantsIslandwalkOnlyToSource() {
        Permanent otherMerrow = addCreatureReady(player1, new DeeptreadMerrow());
        Permanent source = addCreatureReady(player1, new DeeptreadMerrow());
        Permanent opponentMerrow = addCreatureReady(player2, new DeeptreadMerrow());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.ISLANDWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherMerrow, Keyword.ISLANDWALK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentMerrow, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("An Island controlled by the attacker does not prevent blocking")
    void attackersIslandDoesNotPreventBlocking() {
        Permanent merrow = addCreatureReady(player1, new DeeptreadMerrow());
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new DeeptreadMerrow());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gqs.hasKeyword(gd, merrow, Keyword.ISLANDWALK)).isTrue();
    }
}
