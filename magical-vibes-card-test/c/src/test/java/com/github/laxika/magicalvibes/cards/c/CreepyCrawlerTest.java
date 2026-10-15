package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Frightcrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gloom;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreepyCrawler.class, Frightcrawler.class, GrizzlyBears.class, Gloom.class})
class CreepyCrawlerTest extends BaseCardTest {

    @BeforeEach
    void stopAfterCombatDamage() {
        Set<TurnStep> stops = Set.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN,
                TurnStep.DECLARE_BLOCKERS, TurnStep.COMBAT_DAMAGE);
        gd.playerAutoStopSteps.put(player1.getId(), stops);
        gd.playerAutoStopSteps.put(player2.getId(), stops);
    }

    @Test
    @DisplayName("A Horror that entered this turn makes the damaged player afraid")
    void horrorEnteringThisTurnTriggersDiscardAndDraw() {
        GrizzlyBears discardedCard = new GrizzlyBears();
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.enterBattlefieldAndReturn(player1, new Frightcrawler());

        Permanent crawler = addCreatureReady(player1, new CreepyCrawler());
        crawler.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("A Horror attacking the damaged player also makes them afraid")
    void attackingHorrorTriggersDiscardAndDraw() {
        GrizzlyBears discardedCard = new GrizzlyBears();
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent horror = addCreatureReady(player1, new Frightcrawler());
        gd.permanentsEnteredBattlefieldThisTurn.clear();

        horror.setAttacking(true);
        gd.playersAttackedThisTurn.put(horror.getId(), Set.of(player2.getId()));
        Permanent crawler = addCreatureReady(player1, new CreepyCrawler());
        crawler.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Without an afraid condition, combat damage does not trigger")
    void noAfraidConditionDoesNothing() {
        GrizzlyBears retainedCard = new GrizzlyBears();
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(retainedCard));
        harness.setLibrary(player1, List.of(drawnCard));

        Permanent crawler = addCreatureReady(player1, new CreepyCrawler());
        crawler.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retainedCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void attackingHorrorLeavingBattlefieldDoesNotEndFear() {
        GrizzlyBears discardedCard = new GrizzlyBears();
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent horror = addCreatureReady(player1, new Frightcrawler());
        addCreatureReady(player1, new CreepyCrawler());
        gd.permanentsEnteredBattlefieldThisTurn.clear();
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, horror));
        gs.declareBlockers(gd, player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void enteringHorrorLeavingBattlefieldDoesNotEndFearAndEmptyHandDoesNotPreventDraw() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent horror = harness.enterBattlefieldAndReturn(player1, new Frightcrawler());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, horror);
        Permanent crawler = addCreatureReady(player1, new CreepyCrawler());
        crawler.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void faceDownCreatureAttackingMakesOpponentAfraid() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent faceDown = addCreatureReady(player1, new GrizzlyBears());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        faceDown.setAttacking(true);
        gd.playersAttackedThisTurn.put(faceDown.getId(), Set.of(player2.getId()));
        Permanent crawler = addCreatureReady(player1, new CreepyCrawler());
        crawler.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void opponentsEnteringHorrorDoesNotMakeThemAfraidOfCrawlerController() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent horror = harness.enterBattlefieldAndReturn(player2, new Frightcrawler());
        horror.tap();
        Permanent crawler = addCreatureReady(player1, new CreepyCrawler());
        crawler.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void noncreatureEnchantmentEnteringDoesNotMakeOpponentAfraid() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.enterBattlefieldAndReturn(player1, new Gloom());
        Permanent crawler = addCreatureReady(player1, new CreepyCrawler());
        crawler.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
