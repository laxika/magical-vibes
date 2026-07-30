package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeastWalkersTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent walkers = addWalkers(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(walkers.getId());
    }

    @Test
    @DisplayName("Resolving the ability grants banding until end of turn")
    void resolvingGrantsBanding() {
        Permanent walkers = addWalkers(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThat(gqs.hasKeyword(gd, walkers, Keyword.BANDING)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, walkers, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Granted banding wears off at end of turn")
    void bandingWearsOff() {
        Permanent walkers = addWalkers(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, walkers, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without green mana")
    void cannotActivateWithoutGreenMana() {
        addWalkers(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating the ability does not tap Beast Walkers")
    void activatingDoesNotTap() {
        Permanent walkers = addWalkers(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(walkers.isTapped()).isFalse();
    }

    private Permanent addWalkers(Player player) {
        Permanent perm = new Permanent(new BeastWalkers());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
