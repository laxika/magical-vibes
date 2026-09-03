package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
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

@CardUsed({CrimsonHellkite.class, IronTuskElephant.class})
class CrimsonHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Activating puts the ability on the stack with X and target")
    void activatingPutsAbilityOnStack() {
        addHellkiteReady(player1);
        Permanent target = addTargetCreature(player2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 2, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating taps Crimson Hellkite and consumes X mana")
    void activatingTapsAndConsumesMana() {
        Permanent hellkite = addHellkiteReady(player1);
        Permanent target = addTargetCreature(player2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 2, target.getId());

        assertThat(hellkite.isTapped()).isTrue();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving deals X damage and destroys creature when X >= toughness")
    void resolvingDestroysWhenLethal() {
        addHellkiteReady(player1);
        Permanent target = addTargetCreature(player2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 3, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Iron Tusk Elephant");
        harness.assertInGraveyard(player2, "Iron Tusk Elephant");
    }

    @Test
    @DisplayName("Resolving with X below toughness deals damage but does not destroy")
    void resolvingNonLethalLeavesCreature() {
        addHellkiteReady(player1);
        Permanent target = addTargetCreature(player2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Iron Tusk Elephant");
        assertThat(gameLogContains("deals 2 damage")).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addHellkiteReady(player1);
        Permanent target = addTargetCreature(player2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addHellkiteReady(player1);
        Permanent target = addTargetCreature(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot spend non-red mana on X")
    void cannotSpendNonRedManaOnX() {
        Permanent hellkite = addHellkiteReady(player1);
        Permanent target = addTargetCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(hellkite.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateSummoningSick() {
        CrimsonHellkite card = new CrimsonHellkite();
        Permanent perm = new Permanent(card); // summoningSick true by default
        harness.getGameData().playerBattlefields.get(player1.getId()).add(perm);
        Permanent target = addTargetCreature(player2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    private Permanent addHellkiteReady(Player player) {
        return addCreatureReady(player, new CrimsonHellkite());
    }

    private Permanent addTargetCreature(Player player) {
        return addCreatureReady(player, new IronTuskElephant());
    }
}
