package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({AttendedHealer.class, AncestorsProphet.class, GrizzlyBears.class})
class AttendedHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Cat the first time its controller gains life each turn")
    void createsCatOnFirstLifeGainEachTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new AttendedHealer());
        harness.setLife(player1, 20);

        gainLife(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);

        gainLife(1);
        assertThat(gd.stack).isEmpty();

        advanceTurn();
        advanceTurn();
        gainLife(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability grants lifelink to another Cleric until end of turn")
    void grantsLifelinkToAnotherClericUntilEndOfTurn() {
        harness.addToBattlefield(player1, new AttendedHealer());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new AncestorsProphet());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, cleric.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cleric, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cleric, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot target itself or a non-Cleric")
    void activatedAbilityRequiresAnotherCleric() {
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new AttendedHealer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, healer.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void gainLife(int amount) {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), amount));
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
