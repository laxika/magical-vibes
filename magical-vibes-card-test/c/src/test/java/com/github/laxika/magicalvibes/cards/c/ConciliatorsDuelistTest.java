package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConciliatorsDuelist.class, GrizzlyBears.class, HillGiant.class, Shock.class, Assassinate.class})
class ConciliatorsDuelistTest extends BaseCardTest {

    private Permanent addReadyDuelist(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ConciliatorsDuelist());
        perm.setSummoningSick(false);
        return perm;
    }


    @Test
    @DisplayName("ETB draws a card for the controller and makes each player lose 1 life")
    void etbDrawsAndDrainsEachPlayer() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ConciliatorsDuelist()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB triggers

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }


    @Test
    @DisplayName("Casting an instant that targets a creature exiles a chosen creature and schedules its return")
    void reparteeExilesChosenCreature() {
        addReadyDuelist(player1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, giantId);

        // Repartee triggers and prompts for a creature to exile
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getDelayedActions(PendingExileReturn.class))
                .anyMatch(per -> per.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        addReadyDuelist(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    @Test
    @DisplayName("Repartee may target no creatures even when a legal target exists")
    void reparteeCanChooseNoTarget() {
        addReadyDuelist(player1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Conciliator's Duelist");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    @DisplayName("The exiled creature returns under its owner's control at the next end step")
    void reparteeReturnsStolenCreatureToOwner() {
        addReadyDuelist(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player2.getId());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isNotEqualTo(bears.getId());
    }

    @Test
    @DisplayName("An opponent casting an instant targeting a creature does not trigger Repartee")
    void opponentsSpellDoesNotTriggerRepartee() {
        Permanent duelist = addReadyDuelist(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, duelist.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    @Test
    @DisplayName("A sorcery targeting a creature triggers Repartee before that sorcery resolves")
    void sorceryTriggersAndCanExileItsOwnTarget() {
        addReadyDuelist(player1);
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        giant.tap();
        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, giant.getId());
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(harness.getPermanentId(player2, "Hill Giant")).isNotEqualTo(giant.getId());
    }
}
