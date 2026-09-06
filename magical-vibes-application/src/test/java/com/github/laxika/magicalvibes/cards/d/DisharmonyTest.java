package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Disharmony.class, GrizzlyBears.class})
class DisharmonyTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps an attacking creature, removes it from combat, and gains control until end of turn")
    void resolvesAgainstAttackingCreature() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent attacker = addAttacker(player2);
        attacker.tap();

        castDisharmony(attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.isStolenUntilEndOfTurn(attacker.getId())).isTrue();
    }

    @Test
    @DisplayName("Temporary control expires at cleanup")
    void controlExpiresAtCleanup() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent attacker = addAttacker(player2);

        castDisharmony(attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.isStolenUntilEndOfTurn(attacker.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setUpSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Cannot be cast after blockers are declared")
    void cannotCastAfterBlockersAreDeclared() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent attacker = addAttacker(player2);
        setUpSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void castDisharmony(java.util.UUID targetId) {
        setUpSpell();
        harness.castInstant(player1, 0, targetId);
    }

    private void setUpSpell() {
        harness.setHand(player1, List.of(new Disharmony()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.getId().equals(player1.getId()) ? player2.getId() : player1.getId());
        gd.playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }
}
