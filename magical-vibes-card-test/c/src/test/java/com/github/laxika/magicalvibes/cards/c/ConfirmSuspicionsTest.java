package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SilburlindSnapper;
import com.github.laxika.magicalvibes.cards.v.ValMaroonedSurveyor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConfirmSuspicions.class, GrizzlyBears.class, CarnageTyrant.class,
        SilburlindSnapper.class, ValMaroonedSurveyor.class})
class ConfirmSuspicionsTest extends BaseCardTest {

    @Test
    @DisplayName("Counters target spell and investigates three times")
    void countersAndInvestigatesThreeTimes() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ConfirmSuspicions()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player2, "Clue")).hasSize(3);
    }

    @Test
    @DisplayName("Investigates even when the target spell cannot be countered")
    void investigatesWhenTargetCannotBeCountered() {
        CarnageTyrant tyrant = new CarnageTyrant();
        harness.setHand(player1, List.of(tyrant));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ConfirmSuspicions()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player2, 0, tyrant.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Carnage Tyrant");
        assertThat(findPermanents(player2, "Clue")).hasSize(3);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConfirmSuspicions()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each of the three investigations triggers Val separately")
    void triggersForEachInvestigation() {
        harness.addToBattlefield(player2, new ValMaroonedSurveyor());
        SilburlindSnapper snapper = new SilburlindSnapper();
        harness.setHand(player1, List.of(snapper));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        int opponentLife = gd.getLife(player1.getId());
        int controllerLife = gd.getLife(player2.getId());

        harness.setHand(player2, List.of(new ConfirmSuspicions()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player2, 0, snapper.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Silburlind Snapper");
        assertThat(findPermanents(player2, "Clue")).hasSize(3);
        harness.assertLife(player1, opponentLife - 6);
        harness.assertLife(player2, controllerLife + 6);
    }

    @Test
    @DisplayName("Does not investigate when its target has already been countered")
    void doesNotInvestigateWithMissingTarget() {
        SilburlindSnapper snapper = new SilburlindSnapper();
        harness.setHand(player1, List.of(snapper));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ConfirmSuspicions(), new ConfirmSuspicions()));
        harness.addMana(player2, ManaColor.BLUE, 10);
        harness.castInstant(player2, 0, snapper.getId());
        harness.castAndResolveInstant(player2, 0, snapper.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silburlind Snapper");
        assertThat(findPermanents(player2, "Clue")).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A generated Clue can be sacrificed for two mana to draw a card")
    void generatedClueDrawsCard() {
        SilburlindSnapper snapper = new SilburlindSnapper();
        harness.setHand(player1, List.of(snapper));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        SilburlindSnapper drawnCard = new SilburlindSnapper();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player2, List.of(new ConfirmSuspicions()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player2, 0, snapper.getId());
        Permanent clue = findPermanents(player2, "Clue").getFirst();

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        assertThat(findPermanents(player2, "Clue")).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(findPermanents(player2, "Clue")).doesNotContain(clue);
    }
}
