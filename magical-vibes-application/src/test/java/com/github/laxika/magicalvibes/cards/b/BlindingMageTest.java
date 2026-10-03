package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlindingMage.class, RuneclawBear.class, Ornithopter.class, Plains.class})
class BlindingMageTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new BlindingMage());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Blinding Mage")
    void activatingTapsMage() {
        Permanent mage = addCreatureReady(player1, new BlindingMage());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability taps target creature")
    void resolvingTapsTarget() {
        addCreatureReady(player1, new BlindingMage());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap own creature")
    void canTapOwnCreature() {
        addCreatureReady(player1, new BlindingMage());
        Permanent ownBears = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, ownBears.getId());
        harness.passBothPriorities();

        assertThat(ownBears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addCreatureReady(player1, new BlindingMage());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new BlindingMage());
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new BlindingMage());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while Blinding Mage is tapped")
    void cannotActivateWhileTapped() {
        Permanent mage = addCreatureReady(player1, new BlindingMage());
        mage.tap();
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new BlindingMage());
        mage.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(mage.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A noncreature cannot be targeted")
    void cannotTargetNoncreature() {
        Permanent mage = addCreatureReady(player1, new BlindingMage());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(mage.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("An artifact creature is a legal target")
    void canTargetArtifactCreature() {
        addCreatureReady(player1, new BlindingMage());
        Permanent target = addCreatureReady(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blinding Mage can target itself")
    void canTargetItself() {
        Permanent mage = addCreatureReady(player1, new BlindingMage());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, mage.getId());

        assertThat(mage.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(mage.getId());

        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isFalse();
    }

    @Test
    @DisplayName("An already tapped creature remains a legal target")
    void canTargetTappedCreature() {
        addCreatureReady(player1, new BlindingMage());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isFalse();
    }

    @Test
    @DisplayName("The ability resolves after Blinding Mage leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent mage = addCreatureReady(player1, new BlindingMage());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mage);
        harness.setGraveyard(player1, java.util.List.of(mage.getCard()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
