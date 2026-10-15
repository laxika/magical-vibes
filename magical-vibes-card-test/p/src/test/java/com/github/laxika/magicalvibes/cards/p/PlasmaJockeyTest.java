package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Strangle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlasmaJockey.class, GrizzlyBears.class, Strangle.class})
class PlasmaJockeyTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking targets only a creature an opponent controls and makes it unable to block")
    void attackTriggerRestrictsTargets() {
        addCreatureReady(player1, new PlasmaJockey());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Normal casting does not grant haste or sacrifice at the end step")
    void normalCastDoesNotUseBlitz() {
        harness.setHand(player1, List.of(new PlasmaJockey()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jockey = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.hasKeyword(gd, jockey, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jockey);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new PlasmaJockey()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jockey = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.hasKeyword(gd, jockey, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jockey);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Plasma Jockey");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blitz grants haste immediately without an enter-the-battlefield trigger")
    void blitzAppliesDuringSpellResolution() {
        harness.setHand(player1, List.of(new PlasmaJockey()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent jockey = findPermanent(player1, "Plasma Jockey");
        assertThat(gqs.hasKeyword(gd, jockey, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blitz haste remains after cleanup if the permanent remains on the battlefield")
    void blitzHasteDoesNotExpireAtCleanup() {
        harness.setHand(player1, List.of(new PlasmaJockey()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        Permanent jockey = findPermanent(player1, "Plasma Jockey");
        assertThat(gqs.hasKeyword(gd, jockey, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jockey);
        assertThat(gqs.hasKeyword(gd, jockey, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A normally cast Plasma Jockey does not draw when it dies")
    void normalCastDoesNotDrawOnDeath() {
        harness.setHand(player1, List.of(new PlasmaJockey(), new Strangle()));
        harness.setLibrary(player1, List.of(new PlasmaJockey()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castAndResolveSorcery(player1, 0, findPermanent(player1, "Plasma Jockey").getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Plasma Jockey");
        harness.assertNotInHand(player1, "Plasma Jockey");
    }

    @Test
    @DisplayName("A blitzed Plasma Jockey draws when killed before the end step")
    void blitzDrawsOnDeathBeforeEndStep() {
        harness.setHand(player1, List.of(new PlasmaJockey(), new Strangle()));
        harness.setLibrary(player1, List.of(new PlasmaJockey()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        harness.castAndResolveSorcery(player1, 0, findPermanent(player1, "Plasma Jockey").getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Plasma Jockey");
        harness.assertInHand(player1, "Plasma Jockey");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
