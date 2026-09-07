package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RestorationMagic.class, GrizzlyBears.class})
class RestorationMagicTest extends BaseCardTest {

    @Test
    @DisplayName("Cure grants hexproof and indestructible to the target without gaining life")
    void cureProtectsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        prepareCard(0, 1, 0);

        harness.castModalInstant(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HEXPROOF)).isFalse();
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Cura protects the target and gains 3 life")
    void curaProtectsTargetAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        prepareCard(1, 1, 0);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Curaga protects all permanents you control and gains 6 life until end of turn")
    void curagaProtectsControlledPermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        prepareCard(3, 1, 1);

        harness.castModalInstant(player1, 0, 2, List.of());
        harness.passBothPriorities();

        for (Permanent permanent : List.of(first, second)) {
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.HEXPROOF)).isTrue();
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.INDESTRUCTIBLE)).isTrue();
        }
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertLife(player1, 16);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void prepareCard(int colorless, int white, int extraWhite) {
        harness.setHand(player1, List.of(new RestorationMagic()));
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.WHITE, white + extraWhite);
    }
}
