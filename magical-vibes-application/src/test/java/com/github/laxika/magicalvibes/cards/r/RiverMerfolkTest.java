package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiverMerfolk.class, Mountain.class})
class RiverMerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the mountainwalk ability records the source for self-resolution")
    void activatingPutsOnStack() {
        Permanent merfolk = addCreatureReady(player1, new RiverMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(merfolk.getId());
    }

    @Test
    @DisplayName("Resolving grants mountainwalk until end of turn")
    void resolvingGrantsMountainwalk() {
        Permanent merfolk = addCreatureReady(player1, new RiverMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Mountainwalk is conditional on the defending player's Mountain")
    void mountainwalkUsesDefendingPlayersMountain() {
        Permanent merfolk = addCreatureReady(player1, new RiverMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new RiverMerfolk());
        assertThat(bls.canBlockAttacker(gd, blocker, merfolk,
                gd.playerBattlefields.get(player2.getId()))).isTrue();

        harness.addToBattlefield(player2, new Mountain());

        assertThat(bls.canBlockAttacker(gd, blocker, merfolk,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Mountainwalk granted by the ability resets at end of turn cleanup")
    void mountainwalkResetsAtEndOfTurn() {
        Permanent merfolk = addCreatureReady(player1, new RiverMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.MOUNTAINWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without blue mana")
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new RiverMerfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick River Merfolk can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());
        merfolk.setSummoningSick(true);
        merfolk.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.MOUNTAINWALK)).isTrue();
        assertThat(merfolk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability grants mountainwalk only to its own source")
    void grantsMountainwalkOnlyToSource() {
        Permanent otherMerfolk = addCreatureReady(player1, new RiverMerfolk());
        Permanent source = addCreatureReady(player1, new RiverMerfolk());
        Permanent opposingMerfolk = addCreatureReady(player2, new RiverMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.MOUNTAINWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherMerfolk, Keyword.MOUNTAINWALK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingMerfolk, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("A Mountain controlled by the attacker does not prevent blocking")
    void attackersMountainDoesNotEnableMountainwalk() {
        Permanent merfolk = addCreatureReady(player1, new RiverMerfolk());
        harness.addToBattlefield(player1, new Mountain());
        Permanent blocker = addCreatureReady(player2, new RiverMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, merfolk,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
