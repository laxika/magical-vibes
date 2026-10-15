package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArmoredArmadillo;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WylieDukeAtiinHero.class, ArmoredArmadillo.class})
class WylieDukeAtiinHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped gains 1 life and draws a card")
    void becomingTappedGainsLifeAndDrawsCard() {
        Permanent wylie = harness.addToBattlefieldAndReturn(player1, new WylieDukeAtiinHero());
        harness.setLibrary(player1, List.of(new ArmoredArmadillo()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        tap(wylie);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Wylie Duke")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new WylieDukeAtiinHero());
        Permanent armadillo = harness.addToBattlefieldAndReturn(player1, new ArmoredArmadillo());

        tap(armadillo);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vigilance keeps Wylie untapped when attacking and does not trigger")
    void attackingDoesNotTrigger() {
        Permanent wylie = addCreatureReady(player1, new WylieDukeAtiinHero());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(wylie.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Wylie controlled by the opponent rewards that controller")
    void opponentControllerGainsLifeAndDraws() {
        Permanent wylie = harness.addToBattlefieldAndReturn(player2, new WylieDukeAtiinHero());
        harness.setLibrary(player2, List.of(new ArmoredArmadillo()));
        int opponentLife = gd.playerLifeTotals.get(player2.getId());
        int opponentHand = gd.playerHands.get(player2.getId()).size();
        int ourLife = gd.playerLifeTotals.get(player1.getId());
        int ourHand = gd.playerHands.get(player1.getId()).size();

        tap(wylie);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHand + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ourLife);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ourHand);
    }

    @Test
    @DisplayName("Untapping and tapping again triggers again in the same turn")
    void canTriggerTwiceInOneTurn() {
        Permanent wylie = harness.addToBattlefieldAndReturn(player1, new WylieDukeAtiinHero());
        harness.setLibrary(player1, List.of(new ArmoredArmadillo(), new ArmoredArmadillo()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        tap(wylie);
        wylie.untap();
        tap(wylie);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("The tap trigger resolves after Wylie leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent wylie = harness.addToBattlefieldAndReturn(player1, new WylieDukeAtiinHero());
        harness.setLibrary(player1, List.of(new ArmoredArmadillo()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        tap(wylie);
        gd.playerBattlefields.get(player1.getId()).remove(wylie);
        gd.playerGraveyards.get(player1.getId()).add(wylie.getCard());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
