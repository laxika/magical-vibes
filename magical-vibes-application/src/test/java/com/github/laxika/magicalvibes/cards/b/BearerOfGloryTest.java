package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BearerOfGlory.class, GrizzlyBears.class})
class BearerOfGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn")
    void hasFirstStrikeDuringControllersTurn() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new BearerOfGlory());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, bearer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike during its controller's opponent's turn")
    void noFirstStrikeDuringOpponentsTurn() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new BearerOfGlory());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, bearer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Activated ability boosts your creatures until end of turn")
    void activatedAbilityBoostsOwnCreatures() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new BearerOfGlory());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bearer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearer)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }
}
