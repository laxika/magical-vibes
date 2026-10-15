package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellkiteIgniter.class, Ornithopter.class, IchorWellspring.class})
class HellkiteIgniterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent hellkite = addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(hellkite.getId());
    }

    @Test
    @DisplayName("Activating ability does not tap Hellkite Igniter")
    void activatingAbilityDoesNotTap() {
        Permanent hellkite = addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(hellkite.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Gets +0/+0 with no artifacts controlled")
    void getsNoBonusWithNoArtifacts() {
        addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hellkite.getEffectivePower()).isEqualTo(5);
        assertThat(hellkite.getEffectiveToughness()).isEqualTo(5);
        assertThat(hellkite.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +1/+0 with one artifact controlled")
    void getsPlus1With1Artifact() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hellkite.getEffectivePower()).isEqualTo(6);
        assertThat(hellkite.getEffectiveToughness()).isEqualTo(5);
        assertThat(hellkite.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +3/+0 with three artifacts controlled")
    void getsPlus3With3Artifacts() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hellkite.getEffectivePower()).isEqualTo(8);
        assertThat(hellkite.getEffectiveToughness()).isEqualTo(5);
        assertThat(hellkite.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count opponent's artifacts")
    void doesNotCountOpponentArtifacts() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hellkite.getEffectivePower()).isEqualTo(5);
        assertThat(hellkite.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating ability multiple times stacks the bonus")
    void multipleActivationsStack() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // +2 from first activation, +2 from second activation = +4 total
        assertThat(hellkite.getEffectivePower()).isEqualTo(9);
        assertThat(hellkite.getPowerModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hellkite.getEffectivePower()).isEqualTo(7);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hellkite.getPowerModifier()).isEqualTo(0);
        assertThat(hellkite.getEffectivePower()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyHellkite(player1);
        // Only add 1 mana, need {1}{R}
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability resolves without a boost if Hellkite Igniter has left the battlefield")
    void abilityDoesNothingIfSourceRemoved() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove Hellkite Igniter before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).removeFirst();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving ability logs the boost")
    void resolvingAbilityLogsBoost() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gets +1/+0"));
    }

    private Permanent addReadyHellkite(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new HellkiteIgniter());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Counts artifacts on resolution and keeps that bonus after the count changes")
    void artifactCountIsFixedOnResolution() {
        Permanent hellkite = addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IchorWellspring());
        harness.passBothPriorities();

        assertThat(hellkite.getEffectivePower()).isEqualTo(6);
        assertThat(hellkite.getEffectiveToughness()).isEqualTo(5);
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(artifact);
        harness.addToBattlefield(player1, new IchorWellspring());
        harness.addToBattlefield(player1, new IchorWellspring());
        assertThat(hellkite.getEffectivePower()).isEqualTo(6);
    }

    @Test
    @DisplayName("An artifact that leaves before resolution does not contribute to the boost")
    void excludesArtifactRemovedBeforeResolution() {
        Permanent hellkite = addReadyHellkite(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IchorWellspring());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(artifact);

        harness.passBothPriorities();

        assertThat(hellkite.getEffectivePower()).isEqualTo(5);
        assertThat(hellkite.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("An old activation cannot boost Hellkite Igniter after it leaves and returns")
    void doesNotBoostReturnedSource() {
        Permanent original = addReadyHellkite(player1);
        harness.addToBattlefield(player1, new IchorWellspring());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, original.getCard());

        harness.passBothPriorities();

        assertThat(returned.getEffectivePower()).isEqualTo(5);
        assertThat(returned.getEffectiveToughness()).isEqualTo(5);
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
