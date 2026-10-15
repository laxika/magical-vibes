package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.cards.s.SuddenSpoiling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkkiCoalflinger.class, WanderingOnes.class, SuddenSpoiling.class})
class AkkiCoalflingerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants first strike to every attacking creature, including the opponent's")
    void grantsFirstStrikeToAttackers() {
        Permanent coalflinger = addCreatureReady(player1, new AkkiCoalflinger());
        Permanent ownAttacker = addCreatureReady(player1, new WanderingOnes());
        ownAttacker.setAttacking(true);
        Permanent opponentAttacker = addCreatureReady(player2, new WanderingOnes());
        opponentAttacker.setAttacking(true);
        Permanent idleCreature = addCreatureReady(player1, new WanderingOnes());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(coalflinger.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, ownAttacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentAttacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, idleCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void grantWearsOff() {
        addCreatureReady(player1, new AkkiCoalflinger());
        Permanent attacker = addCreatureReady(player1, new WanderingOnes());
        attacker.setAttacking(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Creatures that start attacking after the ability resolves do not gain first strike")
    void grantDoesNotApplyToLaterAttackers() {
        addCreatureReady(player1, new AkkiCoalflinger());
        Permanent lateAttacker = addCreatureReady(player1, new WanderingOnes());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        lateAttacker.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, lateAttacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new AkkiCoalflinger());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Attackers are selected when the ability resolves")
    void selectsAttackersAtResolution() {
        addCreatureReady(player1, new AkkiCoalflinger());
        Permanent stoppedAttacker = addCreatureReady(player1, new WanderingOnes());
        Permanent newAttacker = addCreatureReady(player1, new WanderingOnes());
        stoppedAttacker.setAttacking(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        stoppedAttacker.setAttacking(false);
        newAttacker.setAttacking(true);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, stoppedAttacker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, newAttacker, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike remains after a recipient stops attacking")
    void grantRemainsAfterCombat() {
        addCreatureReady(player1, new AkkiCoalflinger());
        Permanent attacker = addCreatureReady(player1, new WanderingOnes());
        attacker.setAttacking(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        attacker.setAttacking(false);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A later first strike grant survives Sudden Spoiling")
    void grantsFirstStrikeAfterAbilitiesAreRemoved() {
        addCreatureReady(player1, new AkkiCoalflinger());
        Permanent attacker = addCreatureReady(player2, new WanderingOnes());
        declareAttackers(player2, List.of(0));
        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.castAndResolveInstant(player1, 0, player2.getId());
            assertThat(attacker.isAttacking()).isTrue();
            assertThat(gqs.hasLostPrintedAbilities(gd, attacker)).isTrue();
            harness.addMana(player1, ManaColor.RED, 1);
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        });

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
    }
}
