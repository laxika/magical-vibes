package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CommodoreGuff;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalArchmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SparkshaperVisionary.class, TeferiTemporalArchmage.class, CommodoreGuff.class, Forest.class})
class SparkshaperVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Turns any number of your planeswalkers into temporary flying Birds")
    void transformsAnyNumberOfControlledPlaneswalkers() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent teferi = addPlaneswalker(player1, new TeferiTemporalArchmage(), 5);
        Permanent guff = addPlaneswalker(player1, new CommodoreGuff(), 5);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, teferi.getId());
        harness.handlePermanentChosen(player1, guff.getId());
        harness.passBothPriorities();

        for (Permanent planeswalker : List.of(teferi, guff)) {
            assertThat(gqs.isCreature(gd, planeswalker)).isTrue();
            assertThat(gqs.isPlaneswalker(gd, planeswalker)).isFalse();
            assertThat(gqs.getEffectivePower(gd, planeswalker)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, planeswalker)).isEqualTo(3);
            assertThat(gqs.effectiveCreatureSubtypes(gd, planeswalker))
                    .containsExactly(CardSubtype.BIRD);
            assertThat(gqs.hasKeyword(gd, planeswalker, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, planeswalker, Keyword.HEXPROOF)).isTrue();
            assertThat(gqs.getEffectiveColors(gd, planeswalker)).containsExactly(CardColor.BLUE);
        }

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gqs.isCreature(gd, teferi)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, teferi)).isTrue();
        assertThat(gqs.hasKeyword(gd, teferi, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, teferi, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The granted ability scries when the animated planeswalker deals combat damage")
    void combatDamageTriggersScry() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent teferi = addPlaneswalker(player1, new TeferiTemporalArchmage(), 5);
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, teferi.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(1));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Cannot choose an opponent's planeswalker")
    void cannotTargetOpponentPlaneswalker() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent ownTeferi = addPlaneswalker(player1, new TeferiTemporalArchmage(), 5);
        Permanent opponentTeferi = addPlaneswalker(player2, new TeferiTemporalArchmage(), 5);

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentTeferi.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, ownTeferi.getId());
        harness.passBothPriorities();
    }

    @Test
    void canChooseZeroPlaneswalkers() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent teferi = addPlaneswalker(player1, new TeferiTemporalArchmage(), 5);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isPlaneswalker(gd, teferi)).isTrue();
        assertThat(gqs.isCreature(gd, teferi)).isFalse();
    }

    @Test
    void canStopAfterChoosingOneOfSeveralPlaneswalkers() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent teferi = addPlaneswalker(player1, new TeferiTemporalArchmage(), 5);
        Permanent guff = addPlaneswalker(player1, new CommodoreGuff(), 5);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, teferi.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, teferi)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, teferi)).isFalse();
        assertThat(gqs.isCreature(gd, guff)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, guff)).isTrue();
    }

    @Test
    void doesNotTriggerOnOpponentsTurn() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent teferi = addPlaneswalker(player1, new TeferiTemporalArchmage(), 5);

        advanceToCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.isPlaneswalker(gd, teferi)).isTrue();
        assertThat(gqs.isCreature(gd, teferi)).isFalse();
    }

    @Test
    void newlyEnteredPlaneswalkerCannotAttackAfterAnimation() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent teferi = addPlaneswalker(player1, new TeferiTemporalArchmage(), 5);
        teferi.setSummoningSick(true);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, teferi.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, teferi)).isTrue();
        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void animatedPlaneswalkerCanStillActivateLoyaltyAbilityInMainPhase() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent teferi = addPlaneswalker(player1, new TeferiTemporalArchmage(), 5);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, teferi.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        teferi.tap();
        harness.activateAbilityWithMultiTargets(player1, 1, 1, List.of(teferi.getId()));
        harness.passBothPriorities();

        assertThat(teferi.isTapped()).isFalse();
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, teferi)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, teferi)).isFalse();
    }

    @Test
    void animationPreservesCountersAndZeroLoyaltyCreatureSurvivesUntilCleanup() {
        addCreatureReady(player1, new SparkshaperVisionary());
        Permanent teferi = addPlaneswalker(player1, new TeferiTemporalArchmage(), 5);
        teferi.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, teferi.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, teferi)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, teferi)).isEqualTo(4);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        teferi.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(teferi);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(teferi);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(teferi.getCard());
    }

    private Permanent addPlaneswalker(Player player, Card card, int loyalty) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
