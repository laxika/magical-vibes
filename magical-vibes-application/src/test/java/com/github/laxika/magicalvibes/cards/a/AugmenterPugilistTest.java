package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AugmenterPugilist.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class AugmenterPugilistTest extends BaseCardTest {

    @Test
    void getsPlusFivePlusFiveWithEightLands() {
        Permanent augmenter = addCreatureReady(player1, new AugmenterPugilist());
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        int powerBefore = gqs.getEffectivePower(gd, augmenter);
        int toughnessBefore = gqs.getEffectiveToughness(gd, augmenter);
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, augmenter)).isEqualTo(powerBefore + 5);
        assertThat(gqs.getEffectiveToughness(gd, augmenter)).isEqualTo(toughnessBefore + 5);
    }

    @Test
    void echoingEquationCopiesOnlyOtherControlledCreaturesWithoutLegendary() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        TestCards.mutableCard(target).setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent other = addCreatureReady(player1, new HillGiant());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new AugmenterPugilist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(other.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(other.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(opponentCreature.getCard().getName()).isEqualTo("Hill Giant");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(other.getCard().getName()).isEqualTo("Hill Giant");
    }

    @Test
    void echoingEquationCannotTargetAnOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new AugmenterPugilist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castModalSorcery(
                player1, 0, 1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }
}
