package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheUnagiOfKyoshiIsland.class, GrizzlyBears.class, Shock.class, ProdigalPyromancer.class})
class TheUnagiOfKyoshiIslandTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when an opponent draws their second card of the turn")
    void drawsTwoOnOpponentsSecondDraw() {
        harness.addToBattlefield(player1, new TheUnagiOfKyoshiIsland());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        draw(player2);
        draw(player2);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        draw(player2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward can be paid by tapping artifacts or creatures")
    void wardCanBePaidWithWaterbend() {
        Permanent unagi = harness.addToBattlefieldAndReturn(player1, new TheUnagiOfKyoshiIsland());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, unagi.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(Permanent::isTapped)
                .hasSize(4);
        assertThat(unagi.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward counters the spell when waterbend cannot be paid")
    void wardCountersWhenWaterbendCannotBePaid() {
        Permanent unagi = harness.addToBattlefieldAndReturn(player1, new TheUnagiOfKyoshiIsland());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, unagi.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(unagi.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not trigger on its controller's draw or an opponent's first draw")
    void doesNotTriggerOnControllerOrFirstOpponentDraw() {
        harness.addToBattlefield(player1, new TheUnagiOfKyoshiIsland());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        draw(player1);
        draw(player2);

        assertThat(gd.stack).isEmpty();
    }

    private void beginOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TheUnagiOfKyoshiIsland()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Unagi of Kyoshi Island");
    }

    @Test
    @DisplayName("Counts the opponent's first draw even before entering the battlefield")
    void countsDrawBeforeEnteringBattlefield() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        draw(player2);
        harness.addToBattlefield(player1, new TheUnagiOfKyoshiIsland());

        draw(player2);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Does not trigger retroactively when entering after the second draw")
    void doesNotTriggerAfterSecondDrawAlreadyHappened() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        draw(player2);
        draw(player2);
        harness.addToBattlefield(player1, new TheUnagiOfKyoshiIsland());

        draw(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward counters a spell when its controller declines an affordable payment")
    void wardPaymentCanBeDeclined() {
        Permanent unagi = harness.addToBattlefieldAndReturn(player1, new TheUnagiOfKyoshiIsland());
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castInstant(player2, 0, unagi.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(unagi.getMarkedDamage()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Ward accepts a mix of mana and summoning-sick creatures")
    void wardAcceptsManaAndSummoningSickCreatures() {
        Permanent unagi = harness.addToBattlefieldAndReturn(player1, new TheUnagiOfKyoshiIsland());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, unagi.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(unagi.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Accepting ward payment lets the payer choose which creatures to tap")
    void wardLetsPayerChooseWaterbendPermanents() {
        Permanent unagi = harness.addToBattlefieldAndReturn(player1, new TheUnagiOfKyoshiIsland());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, unagi.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(Permanent::isTapped);
        assertThat(unagi.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ward counters an opponent's targeted activated ability")
    void wardCountersOpponentsActivatedAbility() {
        Permanent unagi = harness.addToBattlefieldAndReturn(player1, new TheUnagiOfKyoshiIsland());
        addCreatureReady(player2, new ProdigalPyromancer());
        beginOpponentTurn();

        harness.activateAbility(player2, 0, null, unagi.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(unagi.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's targeted spell")
    void wardDoesNotTriggerForControllersSpell() {
        Permanent unagi = harness.addToBattlefieldAndReturn(player1, new TheUnagiOfKyoshiIsland());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, unagi.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(unagi.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
    }
}
