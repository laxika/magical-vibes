package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilvergillPeddler.class, GrizzlyBears.class, Forest.class})
class SilvergillPeddlerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped draws a card, then prompts for a discard")
    void becomingTappedDrawsThenDiscards() {
        Permanent peddler = harness.addToBattlefieldAndReturn(player1, new SilvergillPeddler());
        GrizzlyBears bears = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(bears));
        harness.setLibrary(player1, List.of(drawn));

        tap(peddler);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isSameAs(drawn);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger Silvergill Peddler")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SilvergillPeddler());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(bears);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping an opponent's creature does not trigger Silvergill Peddler")
    void tappingOpponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SilvergillPeddler());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(bears);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the tapped copy triggers when you control multiple Peddlers")
    void onlyTappedCopyTriggers() {
        Permanent peddler = harness.addToBattlefieldAndReturn(player1, new SilvergillPeddler());
        harness.addToBattlefield(player1, new SilvergillPeddler());

        tap(peddler);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("With an empty hand, the newly drawn card must be discarded")
    void emptyHandStillDrawsThenDiscards() {
        Permanent peddler = harness.addToBattlefieldAndReturn(player1, new SilvergillPeddler());
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        tap(peddler);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("The triggered ability resolves after Silvergill Peddler leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent peddler = harness.addToBattlefieldAndReturn(player1, new SilvergillPeddler());
        Forest held = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(held));
        harness.setLibrary(player1, List.of(drawn));

        tap(peddler);
        gd.playerBattlefields.get(player1.getId()).remove(peddler);
        gd.playerGraveyards.get(player1.getId()).add(peddler.getCard());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held, drawn);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Attacking taps Silvergill Peddler and triggers its draw and discard")
    void attackingTriggersLoot() {
        Permanent peddler = harness.addToBattlefieldAndReturn(player1, new SilvergillPeddler());
        peddler.setSummoningSick(false);
        Forest held = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(held));
        harness.setLibrary(player1, List.of(drawn));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(peddler.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held, drawn);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(held);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }

}
