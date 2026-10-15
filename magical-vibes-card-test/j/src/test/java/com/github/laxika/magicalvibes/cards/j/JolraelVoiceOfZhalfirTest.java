package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JolraelVoiceOfZhalfir.class, Forest.class, GrizzlyBears.class, DryadArbor.class})
class JolraelVoiceOfZhalfirTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, a land you control becomes a Bird based on your hand size")
    void animatesTargetLandBasedOnHandSize() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        advanceToCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(forest.getId(), player1.getId());

        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, forest))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The beginning-of-combat target is limited to a land you control")
    void onlyTargetsOwnLands() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownForest.getId(), player1.getId())
                .doesNotContain(ownBear.getId(), opponentForest.getId());
    }

    @Test
    @DisplayName("The land animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        advanceToCombat();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(forest.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, forest)).isEmpty();
    }

    @Test
    @DisplayName("Combat damage from an animated land draws a card")
    void animatedLandCombatDamageDrawsCard() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToCombat();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void canChooseNoLand() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        advanceToCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void handSizeIsDeterminedAtResolutionAndThenLockedIn() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        advanceToCombat();
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);

        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
    }

    @Test
    void emptyHandMakesLandDieAsZeroToughnessCreature() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Forest card = new Forest();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player1, List.of());

        advanceToCombat();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void ordinaryCreatureCombatDamageDoesNotDraw() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        addCreatureReady(player1, new GrizzlyBears());
        Forest libraryCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void doesNotAnimateOnOpponentsTurn() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    void drawsOnceForEachLandCreatureIncludingLandsNotAnimatedByJolrael() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        addCreatureReady(player1, new DryadArbor());
        addCreatureReady(player1, new DryadArbor());
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        declareAttackers(List.of(1, 2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void opposingLandCreatureDoesNotTriggerDraw() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        addCreatureReady(player2, new DryadArbor());
        Forest libraryCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    private void advanceToCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
