package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UsherOfTheFallen.class})
class UsherOfTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Boast creates a 1/1 white Human Warrior token")
    void boastCreatesHumanWarriorToken() {
        Permanent usher = addCreatureReady(player1, new UsherOfTheFallen());
        usher.setAttackedThisTurn(true);
        addBoastMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human Warrior");
        assertThat(countPermanents(player1, "Human Warrior")).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.WARRIOR);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boast requires Usher of the Fallen to have attacked this turn")
    void boastRequiresThisCreatureToHaveAttacked() {
        addCreatureReady(player1, new UsherOfTheFallen());
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent usher = addCreatureReady(player1, new UsherOfTheFallen());
        usher.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    private void addBoastMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Boast cannot be activated again while its first activation is on the stack")
    void boastLimitAppliesBeforeResolution() {
        Permanent usher = addCreatureReady(player1, new UsherOfTheFallen());
        usher.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Human Warrior")).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Usher has its own boast activation limit")
    void eachPermanentCanBoast() {
        Permanent first = addCreatureReady(player1, new UsherOfTheFallen());
        Permanent second = addCreatureReady(player1, new UsherOfTheFallen());
        first.setAttackedThisTurn(true);
        second.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Warrior")).isEqualTo(2);
    }

    @Test
    @DisplayName("Boast resolves after its source leaves the battlefield")
    void boastResolvesWithoutSource() {
        Permanent usher = addCreatureReady(player1, new UsherOfTheFallen());
        usher.setAttackedThisTurn(true);
        addBoastMana();

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(usher);
        gd.playerGraveyards.get(player1.getId()).add(usher.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Warrior")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An attacking tapped Usher can boast without untapping")
    void tappedUsherCanBoast() {
        Permanent usher = addCreatureReady(player1, new UsherOfTheFallen());
        usher.setAttackedThisTurn(true);
        usher.tap();
        addBoastMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Warrior")).isEqualTo(1);
        assertThat(usher.isTapped()).isTrue();
    }
}
