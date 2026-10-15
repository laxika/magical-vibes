package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MephiticDraught.class, Forest.class, Shatter.class})
class MephiticDraughtTest extends BaseCardTest {

    @Test
    void enteringBattlefieldDrawsACardAndLosesLife() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new MephiticDraught()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void beingPutIntoGraveyardFromBattlefieldDrawsACardAndLosesLife() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new MephiticDraught());
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        var targetId = harness.getPermanentId(player1, "Mephitic Draught");
        harness.castInstant(player2, 0, targetId);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }
    @Test
    void sacrificeDrawsForControllerRatherThanOwner() {
        MephiticDraught draught = new MephiticDraught();
        draught.setOwnerId(player1.getId());
        var permanent = harness.addToBattlefieldAndReturn(player2, draught);
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, permanent));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mephitic Draught");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void entryTriggerStillResolvesAfterDestruction() {
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new MephiticDraught()));
        harness.setHand(player2, List.of(new Shatter()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Mephitic Draught"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mephitic Draught");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void exileDoesNotDrawOrLoseLife() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new MephiticDraught());
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, permanent));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().equals(permanent.getCard()));
    }
}
