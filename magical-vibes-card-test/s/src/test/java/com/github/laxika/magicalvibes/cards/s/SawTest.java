package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalemurkLeech;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Saw.class, BalemurkLeech.class})
class SawTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Saw and gives the creature +2/+0")
    void equipBoostsCreature() {
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new Saw());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalemurkLeech());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(saw.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Moving Saw does not exclude its new host from the original attack trigger")
    void attackTriggerSacrificesOtherPermanentAndDraws() {
        Permanent attacker = addCreatureReady(player1, new BalemurkLeech());
        Permanent currentHost = addCreatureReady(player1, new BalemurkLeech());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BalemurkLeech());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new Saw());
        saw.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new BalemurkLeech()));

        declareAttackers(List.of(0));
        saw.setAttachedTo(currentHost.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(currentHost.getId(), sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Balemurk Leech");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Balemurk Leech");
    }

    @Test
    @DisplayName("Declining the attack trigger does not sacrifice or draw")
    void decliningAttackTriggerDoesNothing() {
        Permanent attacker = addCreatureReady(player1, new BalemurkLeech());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BalemurkLeech());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new Saw());
        saw.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new BalemurkLeech()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotInHand(player1, "Balemurk Leech");
    }

    @Test
    @DisplayName("A normal attack can sacrifice another Equipment but not Saw or the attacker")
    void attackCanSacrificeAnotherEquipment() {
        Permanent attacker = addCreatureReady(player1, new BalemurkLeech());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new Saw());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Saw());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new Saw());
        saw.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new BalemurkLeech()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker, saw).doesNotContain(sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentPermanent);
        harness.assertInGraveyard(player1, "Saw");
        harness.assertInHand(player1, "Balemurk Leech");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting with no eligible permanent does not draw")
    void noEligiblePermanentDoesNotDraw() {
        Permanent attacker = addCreatureReady(player1, new BalemurkLeech());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new Saw());
        saw.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new BalemurkLeech()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(attacker, saw);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotInHand(player1, "Balemurk Leech");
    }

    @Test
    @DisplayName("An unequipped Saw does not trigger when a creature attacks")
    void unequippedSawDoesNotTrigger() {
        addCreatureReady(player1, new BalemurkLeech());
        harness.addToBattlefield(player1, new Saw());
        harness.setLibrary(player1, List.of(new BalemurkLeech()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotInHand(player1, "Balemurk Leech");
    }

    @Test
    @DisplayName("Saw's controller sacrifices and draws when an opponent controls the attacker")
    void equipmentControllerReceivesAttackTrigger() {
        Permanent attacker = addCreatureReady(player2, new BalemurkLeech());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BalemurkLeech());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new Saw());
        saw.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new BalemurkLeech()));
        harness.setLibrary(player2, List.of(new Saw()));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Balemurk Leech");
        harness.assertInHand(player1, "Balemurk Leech");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(saw.getAttachedTo()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("The sacrifice and draw occur during the same resolution without another priority window")
    void sacrificeDrawsImmediatelyWithoutAnotherTrigger() {
        Permanent attacker = addCreatureReady(player1, new BalemurkLeech());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BalemurkLeech());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new Saw());
        saw.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new BalemurkLeech()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(gd.currentStep,
                () -> harness.handlePermanentChosen(player1, sacrifice.getId()));

        harness.assertInGraveyard(player1, "Balemurk Leech");
        harness.assertInHand(player1, "Balemurk Leech");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
