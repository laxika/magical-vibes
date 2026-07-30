package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Craterhoof Behemoth")
class CraterhoofBehemothTest extends BaseCardTest {

    private void castBehemoth() {
        harness.setHand(player1, new ArrayList<>(List.of(new CraterhoofBehemoth())));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    @Test
    @DisplayName("ETB pumps own creatures by the number of creatures you control and grants trample")
    void etbPumpsAndGrantsTrample() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBehemoth();

        Permanent hoof = findPermanent(player1, "Craterhoof Behemoth");

        // Three creatures on the battlefield when the trigger resolves, Craterhoof included.
        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);
        assertThat(otherBears.getPowerModifier()).isEqualTo(3);
        assertThat(hoof.getPowerModifier()).isEqualTo(3);
        assertThat(hoof.getToughnessModifier()).isEqualTo(3);

        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(hoof.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Alone on the battlefield it still pumps itself by one")
    void pumpsItselfWhenAlone() {
        castBehemoth();

        Permanent hoof = findPermanent(player1, "Craterhoof Behemoth");

        assertThat(hoof.getPowerModifier()).isEqualTo(1);
        assertThat(hoof.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not affect creatures an opponent controls")
    void doesNotAffectOpponentCreatures() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBehemoth();

        assertThat(opponentBears.getPowerModifier()).isEqualTo(0);
        assertThat(opponentBears.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Pump and trample wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBehemoth();

        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
