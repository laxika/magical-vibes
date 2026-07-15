package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JandorsSaddlebagsTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped target creature")
    void untapsTappedCreature() {
        addReadySaddlebags(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating taps the Saddlebags and puts the ability on the stack")
    void activatingTapsAndPutsOnStack() {
        Permanent saddlebags = addReadySaddlebags(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(saddlebags.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        addReadySaddlebags(player1);
        Permanent own = addReadyCreature(player1);
        own.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, own.getId());
        harness.passBothPriorities();

        assertThat(own.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadySaddlebags(player1);
        Permanent otherSaddlebags = new Permanent(new JandorsSaddlebags());
        gd.playerBattlefields.get(player2.getId()).add(otherSaddlebags);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // The effect's PermanentIsCreaturePredicate (carried on its TargetSpec, and exposed via
        // targetPredicate() for trigger-target collection) is enforced by the declarative spec
        // interpreter, which rejects the non-creature with the engine-standard predicate message.
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherSaddlebags.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadySaddlebags(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent saddlebags = addReadySaddlebags(player1);
        saddlebags.tap();
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadySaddlebags(Player player) {
        Permanent perm = new Permanent(new JandorsSaddlebags());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
