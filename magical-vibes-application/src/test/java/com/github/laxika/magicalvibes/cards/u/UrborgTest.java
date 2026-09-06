package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
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

@CardUsed({Urborg.class, WhiteKnight.class, BogWraith.class})
class UrborgTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for black mana")
    void tapsForBlackMana() {
        harness.addToBattlefield(player1, new Urborg());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes first strike from the target until end of turn")
    void removesFirstStrike() {
        Permanent urborg = setUpUrborg();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WhiteKnight());

        activate(urborg, target, "It loses first strike");

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();

        endTurn();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Removes swampwalk from the target until end of turn")
    void removesSwampwalk() {
        Permanent urborg = setUpUrborg();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BogWraith());

        activate(urborg, target, "It loses swampwalk");

        assertThat(gqs.hasKeyword(gd, target, Keyword.SWAMPWALK)).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNoncreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new Urborg());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent setUpUrborg() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new Urborg());
    }

    private void activate(Permanent urborg, Permanent target, String mode) {
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(urborg), 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
