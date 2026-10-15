package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EmberwildeAugur;
import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.cards.m.MoltenDisaster;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyromancersSwath.class, Ghostfire.class, MoltenDisaster.class, NessianCourser.class,
        EmberwildeAugur.class})
class PyromancersSwathTest extends BaseCardTest {

    @Test
    @DisplayName("An instant you control deals two extra damage to a player")
    void instantDealsTwoExtraDamageToPlayer() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("An opponent's instant does not get the damage bonus")
    void opponentsInstantIsNotBoosted() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A sorcery you control deals two extra damage to a permanent")
    void sorceryDealsTwoExtraDamageToPermanent() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.addToBattlefield(player2, new NessianCourser());
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertInGraveyard(player2, "Nessian Courser");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Damage from an activated ability is not boosted")
    void activatedAbilityDamageIsNotBoosted() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        addCreatureReady(player1, new EmberwildeAugur());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Emberwilde Augur");
    }

    @Test
    @DisplayName("Combat damage is not boosted")
    void combatDamageIsNotBoosted() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        addCreatureReady(player1, new NessianCourser());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The controller's end step discards their entire hand")
    void controllerEndStepDiscardsHand() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.setHand(player1, List.of(new NessianCourser(), new NessianCourser()));

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Nessian Courser"))
                .hasSize(2);
    }

    @Test
    @DisplayName("The end-step discard does not affect an opponent's hand")
    void opponentEndStepDoesNotDiscardHand() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.setHand(player2, List.of(new NessianCourser()));

        advanceToEndStepTrigger(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The opponent's end step also discards the controller's entire hand")
    void opponentEndStepDiscardsControllerHand() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.setHand(player1, List.of(new NessianCourser(), new Ghostfire()));
        harness.setHand(player2, List.of(new NessianCourser()));

        advanceToEndStepTrigger(player2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Nessian Courser");
        harness.assertInGraveyard(player1, "Ghostfire");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Swaths each add two damage")
    void multipleSwathsStackDamageBonus() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("A spell's damage to its own controller is also increased")
    void selfDamageIsBoosted() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Zero damage is not increased")
    void zeroDamageIsNotBoosted() {
        harness.addToBattlefield(player1, new PyromancersSwath());
        harness.addToBattlefield(player2, new NessianCourser());
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(findPermanent(player2, "Nessian Courser").getMarkedDamage()).isZero();
    }

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
