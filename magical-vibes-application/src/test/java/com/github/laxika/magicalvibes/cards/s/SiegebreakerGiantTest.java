package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SiegebreakerGiant.class, GrizzlyBears.class, FountainOfYouth.class})
class SiegebreakerGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability makes a target creature unable to block this turn")
    void resolvingAbilityMakesTargetCreatureUnableToBlock() {
        addReadyGiant(player1);
        Permanent target = addReadyCreature(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The ability can target an opponent's creature")
    void abilityCanTargetOpponentsCreature() {
        addReadyGiant(player1);
        Permanent target = addReadyCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The restriction wears off at end of turn")
    void restrictionWearsOffAtEndOfTurn() {
        addReadyGiant(player1);
        Permanent target = addReadyCreature(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getBlockLegalityService().canBlock(gd, target)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(harness.getBlockLegalityService().canBlock(gd, target)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadyGiant(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The affected creature cannot be declared as a blocker")
    void affectedCreatureCannotBlock() {
        addReadyGiant(player1);
        Permanent target = addReadyCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getBlockLegalityService().canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("The affected creature can still be blocked when attacking")
    void affectedCreatureCanStillBeBlocked() {
        addReadyGiant(player1);
        Permanent target = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getBlockLegalityService().canBlockAttacker(
                gd, blocker, target, gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("The ability works while the Giant is tapped and summoning sick")
    void abilityDoesNotRequireTappingOrHaste() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new SiegebreakerGiant());
        giant.setTapped(true);
        giant.setSummoningSick(true);
        Permanent target = addReadyCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(giant.isTapped()).isTrue();
    }
    private Permanent addReadyGiant(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SiegebreakerGiant());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
