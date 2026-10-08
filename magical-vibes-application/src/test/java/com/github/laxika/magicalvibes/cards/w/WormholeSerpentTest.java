package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WormholeSerpent.class, GrizzlyBears.class, FountainOfYouth.class})
class WormholeSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature can't be blocked this turn")
    void targetCreatureCannotBeBlockedThisTurn() {
        addCreatureReady(player1, new WormholeSerpent());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new WormholeSerpent());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new WormholeSerpent());
        Permanent target = addCreatureReady(player2, new FountainOfYouth());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Serpent can target itself")
    void tappedSummoningSickSerpentCanTargetItself() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new WormholeSerpent());
        serpent.setSummoningSick(true);
        serpent.setTapped(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, serpent.getId());
        assertThat(serpent.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isTrue();
        assertThat(serpent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can target an opposing creature")
    void canTargetOpposingCreature() {
        addCreatureReady(player1, new WormholeSerpent());
        Permanent target = addCreatureReady(player2, new WormholeSerpent());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
        target.setAttacking(true);
        prepareDeclareBlockers(player2);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The ability resolves after its source leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent source = addCreatureReady(player1, new WormholeSerpent());
        Permanent target = addCreatureReady(player1, new WormholeSerpent());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The ability requires blue mana")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent serpent = addCreatureReady(player1, new WormholeSerpent());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, serpent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(serpent.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A departed target does not transfer the effect to another creature")
    void departedTargetDoesNotAffectOtherCreatures() {
        Permanent source = addCreatureReady(player1, new WormholeSerpent());
        Permanent target = addCreatureReady(player2, new WormholeSerpent());
        Permanent other = addCreatureReady(player2, new WormholeSerpent());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(source.isCantBeBlocked()).isFalse();
        assertThat(other.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
