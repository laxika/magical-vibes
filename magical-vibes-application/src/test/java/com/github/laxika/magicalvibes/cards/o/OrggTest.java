package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrggTest extends BaseCardTest {

    private Permanent orggReadyToAttack() {
        Permanent orgg = new Permanent(new Orgg());
        orgg.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(orgg);
        return orgg;
    }

    private void beginAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    // ===== Block restriction: can't block power 3 or greater =====

    @Test
    @DisplayName("Can block an attacker with power 2")
    void canBlockPowerTwo() {
        Permanent orgg = new Permanent(new Orgg());
        gd.playerBattlefields.get(player1.getId()).add(orgg);
        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(bears);

        assertThat(gqs.canBlockAttacker(gd, orgg, bears,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Can't block an attacker with power 3")
    void cantBlockPowerThree() {
        Permanent orgg = new Permanent(new Orgg());
        gd.playerBattlefields.get(player1.getId()).add(orgg);
        Permanent hillGiant = new Permanent(new HillGiant()); // 3/3
        gd.playerBattlefields.get(player2.getId()).add(hillGiant);

        assertThat(gqs.canBlockAttacker(gd, orgg, hillGiant,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    // ===== Attack restriction: can't attack if defender has untapped power-3 creature =====

    @Test
    @DisplayName("Can't attack if defending player controls an untapped creature with power 3")
    void cantAttackWhenDefenderControlsUntappedPowerThree() {
        orggReadyToAttack();
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new HillGiant())); // 3/3 untapped

        beginAttack();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack if defending player's only power-3 creature is tapped")
    void canAttackWhenDefenderPowerThreeCreatureIsTapped() {
        orggReadyToAttack();
        Permanent tappedGiant = new Permanent(new HillGiant()); // 3/3
        tappedGiant.tap();
        gd.playerBattlefields.get(player2.getId()).add(tappedGiant);

        beginAttack();

        assertThatCode(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Can attack if defending player controls only creatures with power less than 3")
    void canAttackWhenDefenderControlsOnlyLowPowerCreature() {
        orggReadyToAttack();
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears())); // 2/2

        beginAttack();

        assertThatCode(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .doesNotThrowAnyException();
    }
}
