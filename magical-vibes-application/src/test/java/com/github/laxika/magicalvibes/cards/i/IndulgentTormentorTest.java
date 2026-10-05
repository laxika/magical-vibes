package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AssaultSuit;
import com.github.laxika.magicalvibes.cards.c.CourageousResolve;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndulgentTormentor.class, Forest.class, RuneclawBear.class})
class IndulgentTormentorTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent can let the controller draw a card")
    void opponentLetsControllerDraw() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        addCreatureReady(player1, new IndulgentTormentor());

        resolveTormentorTrigger();
        harness.handleListChoice(player2, ChoiceContext.IndulgentTormentorChoice.DRAW);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The opponent can pay three life")
    void opponentPaysLife() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new IndulgentTormentor());

        resolveTormentorTrigger();
        harness.handleListChoice(player2, ChoiceContext.IndulgentTormentorChoice.payLife(3));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The opponent chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        Permanent first = addCreatureReady(player2, new RuneclawBear());
        addCreatureReady(player2, new RuneclawBear());
        addCreatureReady(player1, new IndulgentTormentor());

        resolveTormentorTrigger();
        harness.handleListChoice(player2, ChoiceContext.IndulgentTormentorChoice.SACRIFICE);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("If sacrifice and payment are impossible, the effect draws automatically")
    void impossibleOptionsArePruned() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        harness.setLife(player2, 2);
        addCreatureReady(player1, new IndulgentTormentor());

        resolveTormentorTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("A sole creature is sacrificed without a second choice")
    void opponentSacrificesOnlyCreature() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        addCreatureReady(player2, new RuneclawBear());
        addCreatureReady(player1, new IndulgentTormentor());

        resolveTormentorTrigger();
        harness.handleListChoice(player2, ChoiceContext.IndulgentTormentorChoice.SACRIFICE);

        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(forest);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.setHand(player1, List.of());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addCreatureReady(player1, new IndulgentTormentor());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(forest);
    }

    @Test
    @CardUsed({AssaultSuit.class})
    @DisplayName("A creature that cannot be sacrificed does not prevent the forced draw")
    void unsacrificableCreatureDoesNotOfferSacrifice() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        harness.setLife(player2, 2);
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new AssaultSuit());
        suit.setAttachedTo(creature.getId());
        addCreatureReady(player1, new IndulgentTormentor());

        resolveTormentorTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertLife(player2, 2);
    }

    @Test
    @CardUsed({CourageousResolve.class})
    @DisplayName("An opponent who cannot lose life cannot pay life to prevent the draw")
    void cannotPayLifeWhileLifeLossIsForbidden() {
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new CourageousResolve()));
        harness.setLife(player2, 5);
        addCreatureReady(player1, new IndulgentTormentor());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        gs.passPriority(gd, player1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        harness.assertLife(player2, 5);
    }

    private void resolveTormentorTrigger() {
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
