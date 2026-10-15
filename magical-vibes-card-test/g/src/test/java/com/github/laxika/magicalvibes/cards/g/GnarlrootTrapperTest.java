package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GnarlrootTrapper.class, LlanowarElves.class, GrizzlyBears.class})
class GnarlrootTrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability pays 1 life and produces mana that casts an Elf creature spell")
    void manaAbilityPaysLifeAndCastsElf() {
        addCreatureReady(player1, new GnarlrootTrapper());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertLife(player1, 19);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("The Elf-only mana cannot pay for a non-Elf creature spell")
    void manaCannotPayForNonElf() {
        addCreatureReady(player1, new GnarlrootTrapper());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Second ability grants deathtouch to an attacking Elf you control")
    void grantsDeathtouchToAttackingElf() {
        addCreatureReady(player1, new GnarlrootTrapper());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        elves.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, elves.getId());
        harness.passBothPriorities();

        assertThat(elves.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Granted deathtouch wears off at end of turn")
    void deathtouchWearsOff() {
        addCreatureReady(player1, new GnarlrootTrapper());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        elves.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, elves.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elves.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A non-attacking Elf is not a legal target")
    void nonAttackingElfIsIllegalTarget() {
        addCreatureReady(player1, new GnarlrootTrapper());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An attacking Elf an opponent controls is not a legal target")
    void opponentAttackingElfIsIllegalTarget() {
        addCreatureReady(player1, new GnarlrootTrapper());
        Permanent enemyElves = addCreatureReady(player2, new LlanowarElves());
        enemyElves.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, enemyElves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("An attacking non-Elf is not a legal target")
    void attackingNonElfIsIllegalTarget() {
        addCreatureReady(player1, new GnarlrootTrapper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An Elf that stops attacking before resolution does not gain deathtouch")
    void targetMustStillBeAttackingAtResolution() {
        addCreatureReady(player1, new GnarlrootTrapper());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        elves.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, elves.getId());
        elves.setAttacking(false);
        harness.passBothPriorities();

        assertThat(elves.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Both abilities require an untapped Trapper")
    void cannotActivateAgainAfterProducingMana() {
        Permanent trapper = addCreatureReady(player1, new GnarlrootTrapper());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        elves.setAttacking(true);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(trapper.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 19);
    }
}
