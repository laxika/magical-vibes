package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExposeTheCulprit.class, ExitSpecialist.class, GrizzlyBears.class})
class ExposeTheCulpritTest extends BaseCardTest {

    @Test
    void turnsTargetFaceDownCreatureFaceUpWithoutPayingItsCost() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();

        castExpose(0, List.of(faceDown.getId()));

        assertThat(faceDown.isFaceDown()).isFalse();
        assertThat(faceDown.isCloaked()).isFalse();
    }

    @Test
    void exilesAnyNumberOfDisguisedCreaturesAndCloaksSelectedCards() {
        Permanent disguised = harness.addToBattlefieldAndReturn(player1, new ExitSpecialist());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castExpose(1, List.of());

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(disguised.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(disguised.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(disguised);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.isCloaked()).isTrue());
    }

    @Test
    void bothModesResolveInOrderAndTheTurnedUpCreatureMayBeRecloaked() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        Permanent disguised = harness.addToBattlefieldAndReturn(player1, new ExitSpecialist());

        castExpose(1, 2, new int[]{0, 1}, List.of(faceDown.getId()));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(disguised.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(disguised.getId()));
        harness.passBothPriorities();

        assertThat(faceDown.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.isCloaked()).isTrue());
    }

    private void castExpose(int modeIndex, List<java.util.UUID> targetIds) {
        castExpose(1, 2, new int[]{modeIndex}, targetIds);
    }

    private void castExpose(int choicesRequired, int choicesMax, int[] modeIndices,
                            List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new ExposeTheCulprit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalInstantWithModes(
                player1, 0, choicesRequired, choicesMax, modeIndices, targetIds);
        harness.passBothPriorities();
        if (!targetIds.isEmpty()) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }
    }
}
