package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SanitationAutomaton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExposeTheCulprit.class, ExitSpecialist.class, SanitationAutomaton.class})
class ExposeTheCulpritTest extends BaseCardTest {

    @Test
    void turnsTargetFaceDownCreatureFaceUpWithoutPayingItsCost() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new SanitationAutomaton());
        faceDown.setFaceDownAsCloaked();

        castExpose(0, List.of(faceDown.getId()));

        assertThat(faceDown.isFaceDown()).isFalse();
        assertThat(faceDown.isCloaked()).isFalse();
    }

    @Test
    void exilesAnyNumberOfDisguisedCreaturesAndCloaksSelectedCards() {
        Permanent disguised = harness.addToBattlefieldAndReturn(player1, new ExitSpecialist());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());

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
    void bothModesTurnUpOpponentCreatureAndRecloakOwnDisguiseCreature() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new SanitationAutomaton());
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

    @Test
    void bothModesCanTurnUpAndRecloakTheSameCreature() {
        Permanent disguised = harness.addToBattlefieldAndReturn(player1, new ExitSpecialist());
        disguised.setFaceDownAsCloaked();

        castExpose(1, 2, new int[]{0, 1}, List.of(disguised.getId()));

        assertThat(disguised.isFaceDown()).isFalse();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(disguised.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(disguised.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .doesNotContain(disguised)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(disguised.getCard());
                    assertThat(permanent.isCloaked()).isTrue();
                });
    }

    @Test
    void cloakedInstantRemainsFaceDownWhenFirstModeResolves() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new ExposeTheCulprit());
        faceDown.setFaceDownAsCloaked();

        castExpose(0, List.of(faceDown.getId()));

        assertThat(faceDown.isFaceDown()).isTrue();
        assertThat(faceDown.isCloaked()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(faceDown);
    }

    @Test
    void recloakedOpponentOwnedCreatureRemainsUnderSpellControllersControl() {
        ExitSpecialist stolenCard = new ExitSpecialist();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, stolenCard);

        castExpose(1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of(stolen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(stolenCard);
                    assertThat(permanent.isCloaked()).isTrue();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void secondModeCloaksMultipleSelectedCreaturesAsNewPermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ExitSpecialist());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ExitSpecialist());

        castExpose(1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .doesNotContain(first, second)
                .allSatisfy(permanent -> assertThat(permanent.isCloaked()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(first.getCard(), second.getCard());
    }

    @Test
    void secondModeAllowsChoosingNoCreatures() {
        Permanent disguised = harness.addToBattlefieldAndReturn(player1, new ExitSpecialist());

        castExpose(1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(disguised);
        assertThat(disguised.isFaceDown()).isFalse();
    }

    @Test
    void secondModeExcludesFaceDownAndOpponentDisguiseCreatures() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new ExitSpecialist());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new ExitSpecialist());
        faceDown.setFaceDownAsCloaked();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ExitSpecialist());

        castExpose(1, List.of());

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(eligible.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(faceDown).doesNotContain(eligible);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponent);
    }

    private void castExpose(int modeIndex, List<java.util.UUID> targetIds) {
        castExpose(1, 2, new int[]{modeIndex}, targetIds);
    }

    private void castExpose(int choicesRequired, int choicesMax, int[] modeIndices,
                            List<java.util.UUID> targetIds) {
        boolean targetsOpponent = gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(permanent -> targetIds.contains(permanent.getId()));
        harness.setHand(player1, List.of(new ExposeTheCulprit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalInstantWithModes(
                player1, 0, choicesRequired, choicesMax, modeIndices, targetIds);
        harness.passBothPriorities();
        if (targetsOpponent) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }
    }
}
