package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnStones;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EccentricPestfinderTurnStones.class, TurnStones.class, Shock.class})
class EccentricPestfinderTurnStonesTest extends BaseCardTest {

    @Test
    @DisplayName("Turn Stones creates a Pest for each opponent under its controller's control")
    void createsPestsForEachOpponent() {
        castTurnStones();

        assertThat(findPestTokens(player1)).hasSize(1);
        assertThat(findPestTokens(player2)).isEmpty();
        Permanent pest = findPestTokens(player1).getFirst();
        assertThat(pest.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(pest.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(pest.getCard().getSubtypes()).contains(CardSubtype.PEST);
    }

    @Test
    @DisplayName("Eccentric Pestfinder becomes prepared at an end step after you gained life")
    void becomesPreparedAfterGainingLife() {
        Permanent pestfinder = addCreatureReady(player1, new EccentricPestfinderTurnStones());
        castTurnStones();
        destroyPestWithShock();

        advanceToEndStep(player1);

        assertThat(pestfinder.isPrepared()).isTrue();
        assertThat(pestfinder.getPreparedSpellCardId()).isNotNull();
    }

    @Test
    @DisplayName("Eccentric Pestfinder does not prepare without life gain")
    void doesNotPrepareWithoutGainingLife() {
        Permanent pestfinder = addCreatureReady(player1, new EccentricPestfinderTurnStones());

        advanceToEndStep(player1);

        assertThat(pestfinder.isPrepared()).isFalse();
        assertThat(pestfinder.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Casting Turn Stones from preparation creates Pests and unprepares Eccentric Pestfinder")
    void castsPreparedSpell() {
        Permanent pestfinder = addCreatureReady(player1, new EccentricPestfinderTurnStones());
        preparePestfinder(pestfinder);
        UUID preparedSpellId = pestfinder.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, preparedSpellId);
        harness.passBothPriorities();

        assertThat(findPestTokens(player1)).hasSize(1);
        assertThat(pestfinder.isPrepared()).isFalse();
        assertThat(pestfinder.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("A Pest's death gains exactly one life for its controller")
    void pestDeathGainsLifeForController() {
        castTurnStones();
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        destroyPestWithShock();

        assertThat(findPestTokens(player1)).isEmpty();
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Life gained during an opponent's turn prepares Pestfinder at that turn's end step")
    void becomesPreparedAtOpponentsEndStep() {
        Permanent pestfinder = addCreatureReady(player1, new EccentricPestfinderTurnStones());
        castTurnStones();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        destroyPestWithShock();

        advanceToEndStep(player2);

        assertThat(pestfinder.isPrepared()).isTrue();
        assertThat(pestfinder.getPreparedSpellCardId()).isNotNull();
    }

    @Test
    @DisplayName("An opponent gaining life does not prepare Pestfinder")
    void opponentsLifeGainDoesNotPrepare() {
        Permanent pestfinder = addCreatureReady(player2, new EccentricPestfinderTurnStones());
        castTurnStones();
        destroyPestWithShock();

        advanceToEndStep(player1);

        assertThat(pestfinder.isPrepared()).isFalse();
        assertThat(pestfinder.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Becoming prepared again while already prepared preserves the same spell copy")
    void repeatedPreparationDoesNotCreateAnotherCopy() {
        Permanent pestfinder = addCreatureReady(player1, new EccentricPestfinderTurnStones());
        preparePestfinder(pestfinder);
        UUID preparedSpellId = pestfinder.getPreparedSpellCardId();

        advanceToEndStep(player1);

        assertThat(pestfinder.isPrepared()).isTrue();
        assertThat(pestfinder.getPreparedSpellCardId()).isEqualTo(preparedSpellId);
    }

    @Test
    @DisplayName("Life gained after the end step begins does not trigger preparation retroactively")
    void lifeGainedDuringEndStepDoesNotPrepare() {
        Permanent pestfinder = addCreatureReady(player1, new EccentricPestfinderTurnStones());
        castTurnStones();
        advanceToEndStep(player1);

        destroyPestWithShock();

        harness.assertLife(player1, 21);
        assertThat(pestfinder.isPrepared()).isFalse();
        assertThat(pestfinder.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Preparation does not let Turn Stones be cast during the end step")
    void preparedSorceryRetainsNormalTiming() {
        Permanent pestfinder = addCreatureReady(player1, new EccentricPestfinderTurnStones());
        preparePestfinder(pestfinder);
        UUID preparedSpellId = pestfinder.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, preparedSpellId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pestfinder.isPrepared()).isTrue();
        assertThat(pestfinder.getPreparedSpellCardId()).isEqualTo(preparedSpellId);
        assertThat(findPestTokens(player1)).isEmpty();
    }

    private void castTurnStones() {
        harness.setHand(player1, List.of(new TurnStones()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private void destroyPestWithShock() {
        Permanent pest = findPestTokens(player1).getFirst();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, pest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void preparePestfinder(Permanent pestfinder) {
        castTurnStones();
        destroyPestWithShock();
        advanceToEndStep(player1);
        assertThat(pestfinder.isPrepared()).isTrue();
    }

    private List<Permanent> findPestTokens(Player player) {
        return findPermanents(player, "Pest").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
