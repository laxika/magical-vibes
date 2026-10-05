package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KithkinGreatheart;
import com.github.laxika.magicalvibes.cards.c.CloudcrownOak;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LashOut.class, Forest.class, KithkinGreatheart.class, CloudcrownOak.class})
class LashOutTest extends BaseCardTest {

    private void keepBothRevealedCards() {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    void emptyCasterLibraryCannotWinClash() {
        prepare();
        harness.addToBattlefield(player2, new KithkinGreatheart());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Kithkin Greatheart"));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInGraveyard(player2, "Kithkin Greatheart");
        harness.assertLife(player2, 20);
    }

    @Test
    void revealedLandWinsAgainstEmptyOpponentLibrary() {
        prepare();
        harness.addToBattlefield(player2, new KithkinGreatheart());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Kithkin Greatheart"));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInGraveyard(player2, "Kithkin Greatheart");
        harness.assertLife(player2, 17);
    }

    @Test
    void removedTargetPreventsClashAndAllDamage() {
        prepare();
        harness.addToBattlefield(player2, new KithkinGreatheart());
        stackClashWinForCaster();
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Kithkin Greatheart"));
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Lash Out");
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void clashCardsMoveOnlyAfterBothPlacementDecisions() {
        prepare();
        harness.addToBattlefield(player2, new CloudcrownOak());
        KithkinGreatheart revealed = new KithkinGreatheart();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Cloudcrown Oak"));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed, next);
    }

    @Test
    void tiedClashDoesNotDamageController() {
        prepare();
        harness.addToBattlefield(player2, new KithkinGreatheart());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Kithkin Greatheart"));
        keepBothRevealedCards();

        harness.assertInGraveyard(player2, "Kithkin Greatheart");
        harness.assertLife(player2, 20);
    }

    @Test
    void winningClashDamagesOwnCreaturesController() {
        prepare();
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new KithkinGreatheart());
        stackClashWinForCaster();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Kithkin Greatheart"));
        keepBothRevealedCards();

        harness.assertInGraveyard(player1, "Kithkin Greatheart");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void activeOpponentChoosesClashPlacementFirst() {
        prepare();
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new CloudcrownOak());
        stackClashWinForCaster();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Cloudcrown Oak"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    void bottomingWinningCardDoesNotChangeClashOutcome() {
        prepare();
        harness.addToBattlefield(player2, new CloudcrownOak());
        KithkinGreatheart revealed = new KithkinGreatheart();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Cloudcrown Oak"));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, revealed);
        harness.assertLife(player2, 17);
    }

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LashOut()));
        harness.addMana(player1, ManaColor.RED, 2); // {1}{R}
        harness.setLife(player2, 20);
    }

    // Caster (player1) wins the clash: their revealed top card (MV 2) beats the opponent's Forest (MV 0).
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    // Caster (player1) loses the clash: the opponent reveals the higher mana value.
    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new KithkinGreatheart(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Winning the clash deals 3 to the creature and 3 to its controller")
    void wonClashDealsDamageToControllerCreatureSurvives() {
        prepare();
        var target = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());
        stackClashWinForCaster();

        UUID targetId = harness.getPermanentId(player2, "Cloudcrown Oak");
        harness.castAndResolveInstant(player1, 0, targetId);
        keepBothRevealedCards();

        // 3/4 survives 3 marked damage, and its controller takes the clash-win 3 damage.
        harness.assertOnBattlefield(player2, "Cloudcrown Oak");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Winning the clash still damages the controller when the creature dies to the 3 damage")
    void wonClashDamagesControllerWhenCreatureDies() {
        prepare();
        harness.addToBattlefield(player2, new KithkinGreatheart());
        stackClashWinForCaster();

        UUID targetId = harness.getPermanentId(player2, "Kithkin Greatheart");
        harness.castAndResolveInstant(player1, 0, targetId);
        keepBothRevealedCards();

        // Lethal damage does not remove the creature until the spell finishes resolving.
        harness.assertNotOnBattlefield(player2, "Kithkin Greatheart");
        harness.assertInGraveyard(player2, "Kithkin Greatheart");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Losing the clash deals 3 to the creature but nothing to its controller")
    void lostClashDealsNoDamageToController() {
        prepare();
        harness.addToBattlefield(player2, new KithkinGreatheart());
        stackClashLossForCaster();

        UUID targetId = harness.getPermanentId(player2, "Kithkin Greatheart");
        harness.castAndResolveInstant(player1, 0, targetId);
        keepBothRevealedCards();

        // Creature still takes the 3 damage and dies; controller is untouched on a loss.
        harness.assertNotOnBattlefield(player2, "Kithkin Greatheart");
        harness.assertInGraveyard(player2, "Kithkin Greatheart");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        prepare();
        harness.addToBattlefield(player1, new KithkinGreatheart()); // valid target so spell is castable
        harness.addToBattlefield(player2, new Forest());

        UUID landId = harness.getPermanentId(player2, "Forest");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }
}
