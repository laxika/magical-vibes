package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMightyThorJaneFoster.class, GrizzlyBears.class, Island.class, LeoninScimitar.class})
class TheMightyThorJaneFosterTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets a nontoken artifact or creature")
    void attackTriggerFiltersTargets() {
        addCreatureReady(player1, new TheMightyThorJaneFoster());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreature.getId(), equipment.getId())
                .doesNotContain(land.getId());
    }

    @Test
    @DisplayName("Attack trigger returns the target tapped under its owner's control")
    void attackTriggerFlickersTargetTapped() {
        addCreatureReady(player1, new TheMightyThorJaneFoster());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned).isNotSameAs(target);
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An Equipment entering under your control draws a card")
    void equipmentEnteringDrawsCard() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new TheMightyThorJaneFoster());

        harness.enterBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The attack trigger may choose no target")
    void attackTriggerMayChooseNoTarget() {
        Permanent thor = addCreatureReady(player1, new TheMightyThorJaneFoster());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "The Mighty Thor, Jane Foster")).isSameAs(thor);
        assertThat(findPermanent(player2, "Grizzly Bears")).isSameAs(target);
    }

    @Test
    @DisplayName("Thor can flicker herself and returns tapped outside combat")
    void attackTriggerCanTargetThor() {
        Permanent thor = addCreatureReady(player1, new TheMightyThorJaneFoster());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, thor.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "The Mighty Thor, Jane Foster");
        assertThat(returned).isNotSameAs(thor);
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Flickering your Equipment draws a card when it returns")
    void flickeringControlledEquipmentDrawsCard() {
        addCreatureReady(player1, new TheMightyThorJaneFoster());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, equipment.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Leonin Scimitar");
        assertThat(returned).isNotSameAs(equipment);
        assertThat(returned.isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opponent Equipment entering and your non-Equipment entering do not draw")
    void unrelatedEntriesDoNotDraw() {
        harness.addToBattlefield(player1, new TheMightyThorJaneFoster());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A borrowed Equipment returns to its owner and does not draw for Thor's controller")
    void borrowedEquipmentReturnsToOwner() {
        addCreatureReady(player1, new TheMightyThorJaneFoster());
        LeoninScimitar card = new LeoninScimitar();
        card.setOwnerId(player2.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(equipment.getId(), player2.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, equipment.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        Permanent returned = findPermanent(player2, "Leonin Scimitar");
        assertThat(returned).isNotSameAs(equipment);
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
