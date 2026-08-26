package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FirebornKnight.class)
class FirebornKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Four red mana activates Fireborn Knight's hybrid pump ability")
    void redManaActivatesPumpAbility() {
        Permanent knight = addReadyKnight();
        addFourMana(ManaColor.RED);

        activatePump();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    @DisplayName("Four white mana activates Fireborn Knight's hybrid pump ability")
    void whiteManaActivatesPumpAbility() {
        Permanent knight = addReadyKnight();
        addFourMana(ManaColor.WHITE);

        activatePump();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fireborn Knight's pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent knight = addReadyKnight();
        addFourMana(ManaColor.RED);

        activatePump();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fireborn Knight deals combat damage in both combat damage steps")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent knight = new Permanent(new FirebornKnight());
        knight.setSummoningSick(false);
        knight.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(knight);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Fireborn Knight cannot activate its pump without four hybrid mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyKnight();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKnight() {
        return addCreatureReady(player1, new FirebornKnight());
    }

    private void addFourMana(ManaColor color) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, color, 4);
    }

    private void activatePump() {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
