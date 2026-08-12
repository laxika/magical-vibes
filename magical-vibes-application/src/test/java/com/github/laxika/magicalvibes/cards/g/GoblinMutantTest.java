package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinMutantTest extends BaseCardTest {

    private Permanent mutant() {
        Permanent mutant = new Permanent(new GoblinMutant());
        mutant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mutant);
        return mutant;
    }

    private void readyToAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    @DisplayName("Can attack when the defending player controls no creatures")
    void canAttackWithEmptyDefenderBoard() {
        harness.setLife(player2, 20);
        mutant();

        readyToAttack();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Can attack when the defending player's power 3 creature is tapped")
    void canAttackWhenBigCreatureTapped() {
        harness.setLife(player2, 20);
        Permanent giant = new Permanent(new HillGiant()); // 3/3
        giant.tap();
        gd.playerBattlefields.get(player2.getId()).add(giant);
        mutant();

        readyToAttack();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Can attack when the defending player's untapped creatures all have power below 3")
    void canAttackWhenOnlySmallUntappedCreatures() {
        harness.setLife(player2, 20);
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new FugitiveWizard())); // 1/1
        Permanent mutant = mutant();

        readyToAttack();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(mutant.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can't attack when the defending player controls an untapped creature with power 3 or greater")
    void cantAttackIntoUntappedBigCreature() {
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new HillGiant())); // 3/3
        mutant();

        readyToAttack();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can't attack when the defending player controls an untapped creature with power greater than 3")
    void cantAttackIntoUntappedCreatureWithPowerGreaterThanThree() {
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new CrawWurm())); // 6/4
        mutant();

        readyToAttack();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block an attacker with power below 3")
    void canBlockSmallAttacker() {
        Permanent mutant = mutant();
        Permanent wizard = new Permanent(new FugitiveWizard()); // 1/1
        gd.playerBattlefields.get(player2.getId()).add(wizard);

        assertThat(bls.canBlockAttacker(gd, mutant, wizard,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Can't block an attacker with power 3 or greater")
    void cantBlockBigAttacker() {
        Permanent mutant = mutant();
        Permanent giant = new Permanent(new HillGiant()); // 3/3
        gd.playerBattlefields.get(player2.getId()).add(giant);

        assertThat(bls.canBlockAttacker(gd, mutant, giant,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Can't block an attacker with power greater than 3")
    void cantBlockAttackerWithPowerGreaterThanThree() {
        Permanent mutant = mutant();
        Permanent wurm = new Permanent(new CrawWurm()); // 6/4
        gd.playerBattlefields.get(player2.getId()).add(wurm);

        assertThat(bls.canBlockAttacker(gd, mutant, wurm,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }
}
