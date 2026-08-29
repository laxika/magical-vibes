package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TyrantsMachine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoldmawChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Boast taps the target creature")
    void boastTapsTargetCreature() {
        Permanent champion = addCreatureReady(player1, new GoldmawChampion());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        champion.setAttackedThisTurn(true);
        addBoastMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(champion.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boast cannot be activated if Goldmaw Champion did not attack this turn")
    void boastRequiresThisCreatureToHaveAttacked() {
        Permanent champion = addCreatureReady(player1, new GoldmawChampion());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
        assertThat(champion.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent champion = addCreatureReady(player1, new GoldmawChampion());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        champion.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boast cannot target a noncreature permanent")
    void boastCannotTargetNonCreature() {
        Permanent champion = addCreatureReady(player1, new GoldmawChampion());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TyrantsMachine());
        champion.setAttackedThisTurn(true);
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBoastMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
