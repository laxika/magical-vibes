package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AlwaysWatching;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HulkingDevil;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.w.WelcomeToTheFold;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NahiriTheHarbinger.class, Forest.class, AlwaysWatching.class, DevilthornFox.class,
        MagnifyingGlass.class, HulkingDevil.class, WelcomeToTheFold.class})
class NahiriTheHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("+2 may discard a card and draw a card")
    void plusTwoDiscardsAndDraws() {
        Permanent nahiri = addReadyNahiri(player1, 3);
        harness.setHand(player1, List.of(new DevilthornFox()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Devilthorn Fox");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("-2 exiles an enchantment")
    void minusTwoExilesEnchantment() {
        addReadyNahiri(player1, 3);
        harness.addToBattlefield(player2, new AlwaysWatching());
        var anthemId = harness.getPermanentId(player2, "Always Watching");

        harness.activateAbility(player1, 0, 1, null, anthemId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Always Watching");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Always Watching"));
    }

    @Test
    @DisplayName("-2 exiles a tapped artifact")
    void minusTwoExilesTappedArtifact() {
        addReadyNahiri(player1, 3);
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MagnifyingGlass());
        mindStone.tap();

        harness.activateAbility(player1, 0, 1, null, mindStone.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Magnifying Glass");
    }

    @Test
    @DisplayName("-2 exiles a tapped creature")
    void minusTwoExilesTappedCreature() {
        addReadyNahiri(player1, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        bears.tap();

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Devilthorn Fox");
    }

    @Test
    @DisplayName("-2 cannot target an untapped artifact")
    void minusTwoCannotTargetUntappedArtifact() {
        addReadyNahiri(player1, 3);
        harness.addToBattlefield(player2, new MagnifyingGlass());
        var mindStoneId = harness.getPermanentId(player2, "Magnifying Glass");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, mindStoneId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-8 puts an artifact or creature onto the battlefield with haste, then returns it to hand")
    void minusEightReturnsFoundPermanentToHandAtNextEndStep() {
        addReadyNahiri(player1, 8);
        harness.setLibrary(player1, List.of(new HulkingDevil(), new Forest()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Hulking Devil");
        Permanent dragon = findPermanent(player1, "Hulking Devil");
        assertThat(dragon.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hulking Devil");
        harness.assertInHand(player1, "Hulking Devil");
    }

    @Test
    void plusTwoCanDeclineDiscardWithoutDrawing() {
        Permanent nahiri = addReadyNahiri(player1, 4);
        harness.setHand(player1, List.of(new DevilthornFox()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertInHand(player1, "Devilthorn Fox");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void plusTwoCannotDrawWithAnEmptyHand() {
        Permanent nahiri = addReadyNahiri(player1, 4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusTwoCannotTargetUntappedCreature() {
        addReadyNahiri(player1, 4);
        Permanent fox = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, fox.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Nahiri, the Harbinger").getCounterCount(CounterType.LOYALTY))
                .isEqualTo(4);
    }

    @Test
    void minusTwoDoesNotExileCreatureThatUntapsBeforeResolution() {
        addReadyNahiri(player1, 4);
        Permanent fox = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        fox.tap();

        harness.activateAbility(player1, 0, 1, null, fox.getId());
        fox.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Devilthorn Fox");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void minusEightCanFindANoncreatureArtifact() {
        addReadyNahiri(player1, 8);
        harness.setLibrary(player1, List.of(new MagnifyingGlass(), new Forest(), new AlwaysWatching()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Magnifying Glass");
        harness.assertNotOnBattlefield(player1, "Nahiri, the Harbinger");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Magnifying Glass");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Magnifying Glass");
        harness.assertInHand(player1, "Magnifying Glass");
    }

    @Test
    void minusEightMayFailToFindEvenWithAnEligibleCard() {
        addReadyNahiri(player1, 8);
        harness.setLibrary(player1, List.of(new HulkingDevil(), new Forest()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Hulking Devil");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void delayedReturnRetainsNahiriAsItsSourceAfterControlChanges() {
        addReadyNahiri(player1, 8);
        harness.setLibrary(player1, List.of(new HulkingDevil()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent devil = findPermanent(player1, "Hulking Devil");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WelcomeToTheFold()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player2, 0, devil.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player2, "Hulking Devil");
        assertThat(gd.stack).singleElement().satisfies(trigger -> {
            assertThat(trigger.getCard().getName()).isEqualTo("Nahiri, the Harbinger");
            assertThat(trigger.getControllerId()).isEqualTo(player1.getId());
        });
    }

    private Permanent addReadyNahiri(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new NahiriTheHarbinger());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
