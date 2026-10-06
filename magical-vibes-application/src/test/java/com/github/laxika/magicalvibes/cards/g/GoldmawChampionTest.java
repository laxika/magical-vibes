package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.r.RavenWings;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldmawChampion.class, BeskirShieldmate.class, RavenWings.class})
class GoldmawChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Boast taps the target creature")
    void boastTapsTargetCreature() {
        Permanent champion = addCreatureReady(player1, new GoldmawChampion());
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
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
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
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
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RavenWings());
        champion.setAttackedThisTurn(true);
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boast can be activated while tapped after declaring an attack")
    void boastAfterDeclaringAttack() {
        Permanent champion = addCreatureReady(player1, new GoldmawChampion());
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        addBoastMana();
        declareAttackers(List.of(0));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(champion.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boast can target Goldmaw Champion itself even when already tapped")
    void boastCanTargetItselfWhileTapped() {
        Permanent champion = addCreatureReady(player1, new GoldmawChampion());
        champion.setAttackedThisTurn(true);
        champion.tap();
        addBoastMana();

        harness.activateAbility(player1, 0, null, champion.getId());
        harness.passBothPriorities();

        assertThat(champion.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boast requires white mana and an unsuccessful attempt does not consume its use")
    void failedManaPaymentDoesNotUseBoast() {
        Permanent champion = addCreatureReady(player1, new GoldmawChampion());
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        champion.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boast resolves even if Goldmaw Champion leaves the battlefield")
    void boastResolvesWithoutSource() {
        Permanent champion = addCreatureReady(player1, new GoldmawChampion());
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        champion.setAttackedThisTurn(true);
        addBoastMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(champion);
        gd.playerGraveyards.get(player1.getId()).add(champion.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boast cannot be activated again while its first activation is on the stack")
    void boastLimitAppliesBeforeResolution() {
        Permanent champion = addCreatureReady(player1, new GoldmawChampion());
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        champion.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boast does not affect a creature that has left the battlefield")
    void boastWithRemovedTarget() {
        Permanent champion = addCreatureReady(player1, new GoldmawChampion());
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        champion.setAttackedThisTurn(true);
        addBoastMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setExile(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addBoastMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
