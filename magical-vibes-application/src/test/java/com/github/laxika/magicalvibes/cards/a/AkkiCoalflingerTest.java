package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkkiCoalflinger.class, WanderingOnes.class})
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
}
