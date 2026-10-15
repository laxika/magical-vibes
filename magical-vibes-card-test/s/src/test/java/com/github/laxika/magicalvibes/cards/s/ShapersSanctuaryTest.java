package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.w.WatertrapWeaver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShapersSanctuary.class, GrizzlyBears.class, Shock.class, ElaborateFirecannon.class,
        Demystify.class, LightningStrike.class, RaptorCompanion.class, SleekSchooner.class,
        WatertrapWeaver.class})
class ShapersSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when opponent casts a spell targeting a creature you control")
    void triggersOnOpponentSpellTargetingCreature() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);

        // Shock + Shapers' Sanctuary triggered ability on stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Shapers' Sanctuary");
    }

    @Test
    @DisplayName("Accepting the trigger draws a card")
    void acceptingDrawsACard() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // resolve Shapers' Sanctuary trigger → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the trigger does not draw a card")
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // resolve Shapers' Sanctuary trigger → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does NOT trigger when controller casts a spell targeting own creature")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bearsId);

        // Only the Shock spell on the stack — no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Triggers when opponent activates an ability targeting a creature you control")
    void triggersOnOpponentAbilityTargetingCreature() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        // Give opponent an Elaborate Firecannon (activated ability that targets any target)
        Permanent firecannon = harness.addToBattlefieldAndReturn(player2, new ElaborateFirecannon());
        firecannon.setSummoningSick(false);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, bearsId);

        // Ability + Shapers' Sanctuary triggered ability on stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Shapers' Sanctuary");
    }

    @Test
    @DisplayName("Does not trigger when an opponent targets Sanctuary itself")
    void doesNotTriggerOnNonCreatureTarget() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new ShapersSanctuary());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, sanctuary.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Demystify");
    }

    @Test
    @DisplayName("Two Shapers' Sanctuaries each trigger separately")
    void twoSanctuariesStack() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);

        // Shock + 2 Shapers' Sanctuary triggers on stack
        assertThat(gd.stack).hasSize(3);

        // Resolve both triggers and accept both
        harness.passBothPriorities(); // resolve first Sanctuary trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve second Sanctuary trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("A crewed Vehicle triggers Sanctuary when targeted by an opponent's spell")
    void triggersForCrewedVehicleTargetedBySpell() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new SleekSchooner());
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        harness.setLibrary(player1, List.of(new RaptorCompanion()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, vehicle.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.stack).hasSize(1);
        assertThat(vehicle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A crewed Vehicle triggers Sanctuary when targeted by an opponent's ability")
    void triggersForCrewedVehicleTargetedByAbility() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new SleekSchooner());
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        harness.addToBattlefield(player2, new ElaborateFirecannon());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, vehicle.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Shapers' Sanctuary");
    }

    @Test
    @DisplayName("Triggers for an opponent's targeted enter-the-battlefield ability")
    void triggersForOpponentTriggeredAbility() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WatertrapWeaver()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player2, 0, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Shapers' Sanctuary");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(creature.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger for an opponent targeting their own creature")
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for the controller's targeted activated ability")
    void doesNotTriggerForControllersAbility() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.addToBattlefield(player1, new ElaborateFirecannon());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 2, null, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for a spell targeting its controller")
    void doesNotTriggerForPlayerTarget() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }
}
