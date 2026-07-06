package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NiblisOfTheBreathTest extends BaseCardTest {

    

    @Test
    @DisplayName("Activating ability puts it on the stack and taps Niblis")
    void activatingPutsAbilityOnStackAndTapsNiblis() {
        Permanent niblis = addReadyNiblis(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(niblis.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Niblis of the Breath");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolves by tapping an untapped target creature")
    void tapsUntappedCreature() {
        addReadyNiblis(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolves by untapping a tapped target creature")
    void untapsTappedCreature() {
        addReadyNiblis(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target a creature controlled by Niblis's controller")
    void canTargetOwnCreature() {
        addReadyNiblis(player1);
        Permanent target = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadyNiblis(player1);
        Permanent artifact = new Permanent(new AngelsFeather());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutBlueMana() {
        addReadyNiblis(player1);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent niblis = addReadyNiblis(player1);
        niblis.tap();
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent niblis = new Permanent(new NiblisOfTheBreath());
        gd.playerBattlefields.get(player1.getId()).add(niblis);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves the battlefield before resolution")
    void fizzlesIfTargetLeavesBattlefield() {
        addReadyNiblis(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addReadyNiblis(Player player) {
        Permanent permanent = new Permanent(new NiblisOfTheBreath());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
