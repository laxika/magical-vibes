package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiverMerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the mountainwalk ability puts it on the stack targeting itself")
    void activatingPutsOnStack() {
        Permanent merfolk = addRiverMerfolkReady(player1);
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
        Permanent merfolk = addRiverMerfolkReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Mountainwalk granted by the ability resets at end of turn cleanup")
    void mountainwalkResetsAtEndOfTurn() {
        Permanent merfolk = addRiverMerfolkReady(player1);
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
        addRiverMerfolkReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addRiverMerfolkReady(Player player) {
        Permanent perm = new Permanent(new RiverMerfolk());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
