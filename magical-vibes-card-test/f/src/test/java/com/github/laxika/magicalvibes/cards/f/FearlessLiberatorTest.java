package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({FearlessLiberator.class})
class FearlessLiberatorTest extends BaseCardTest {

    @Test
    @DisplayName("Boast creates a 2/1 red Dwarf Berserker token")
    void boastCreatesDwarfBerserkerToken() {
        Permanent liberator = addCreatureReady(player1, new FearlessLiberator());
        liberator.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Dwarf Berserker");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DWARF, CardSubtype.BERSERKER);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boast cannot be activated before Fearless Liberator attacks")
    void boastRequiresAttack() {
        addCreatureReady(player1, new FearlessLiberator());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent liberator = addCreatureReady(player1, new FearlessLiberator());
        liberator.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boast can be activated with the attacker tapped during the end step")
    void boastDuringEndStepAfterAttack() {
        Permanent liberator = addCreatureReady(player1, new FearlessLiberator());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(liberator.isAttackedThisTurn()).isTrue();
        assertThat(liberator.isTapped()).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dwarf Berserker")).isEqualTo(1);
        assertThat(countPermanents(player2, "Dwarf Berserker")).isZero();
    }

    @Test
    @DisplayName("Boast needs red mana and failed payment does not consume the activation")
    void boastRequiresRedMana() {
        Permanent liberator = addCreatureReady(player1, new FearlessLiberator());
        liberator.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Dwarf Berserker")).isZero();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dwarf Berserker")).isEqualTo(1);
    }

    @Test
    @DisplayName("Boast's activation limit applies before the ability resolves")
    void cannotBoastAgainWhileFirstActivationIsOnStack() {
        Permanent liberator = addCreatureReady(player1, new FearlessLiberator());
        liberator.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(countPermanents(player1, "Dwarf Berserker")).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Dwarf Berserker")).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Fearless Liberator has its own boast activation limit")
    void boastLimitsAreIndependentForEachPermanent() {
        Permanent first = addCreatureReady(player1, new FearlessLiberator());
        Permanent second = addCreatureReady(player1, new FearlessLiberator());
        first.setAttackedThisTurn(true);
        second.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dwarf Berserker")).isEqualTo(2);
    }

    @Test
    @DisplayName("Boast resolves even if Fearless Liberator leaves the battlefield")
    void boastResolvesWithoutSource() {
        Permanent liberator = addCreatureReady(player1, new FearlessLiberator());
        liberator.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(liberator);
        gd.playerGraveyards.get(player1.getId()).add(liberator.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dwarf Berserker")).isEqualTo(1);
        assertThat(countPermanents(player2, "Dwarf Berserker")).isZero();
    }
}
