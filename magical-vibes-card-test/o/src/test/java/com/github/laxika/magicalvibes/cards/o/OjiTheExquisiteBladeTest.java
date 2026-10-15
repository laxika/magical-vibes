package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OjiTheExquisiteBlade.class, DarkRitual.class, GrizzlyBears.class})
class OjiTheExquisiteBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with 2 life and scry 2")
    void entersWithLifeGainAndScry() {
        Card topCard = new GrizzlyBears();
        Card bottomCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new OjiTheExquisiteBlade(), "{2}{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomCard, topCard);
    }

    @Test
    @DisplayName("The second spell flickers up to one creature you control")
    void secondSpellFlickersTargetCreature() {
        harness.addToBattlefield(player1, new OjiTheExquisiteBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The second-spell trigger may resolve without a target")
    void secondSpellMayChooseNoTarget() {
        harness.addToBattlefield(player1, new OjiTheExquisiteBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Only the second spell triggers, not the first or third")
    void firstAndThirdSpellsDoNotTrigger() {
        harness.addToBattlefield(player1, new OjiTheExquisiteBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        var returnedId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThat(returnedId).isNotEqualTo(creature.getId());
        harness.passBothPriorities();

        harness.castInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(returnedId);
    }

    @Test
    @DisplayName("The opponent's second spell does not trigger Oji")
    void opponentsSpellsDoNotTrigger() {
        harness.addToBattlefield(player1, new OjiTheExquisiteBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player2, new DarkRitual(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player2, new DarkRitual(), "{B}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The controller's second spell also triggers during an opponent's turn")
    void secondSpellOnOpponentsTurnTriggers() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new OjiTheExquisiteBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Oji can flicker itself and trigger its enters ability again")
    void flickeringOjiTriggersLifeGainAgain() {
        Permanent oji = harness.addToBattlefieldAndReturn(player1, new OjiTheExquisiteBlade());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 10);

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.handlePermanentChosen(player1, oji.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Oji, the Exquisite Blade")).isNotEqualTo(oji.getId());
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Spells cast before Oji entered still count toward the second spell")
    void earlierSpellCountsBeforeOjiEnters() {
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new OjiTheExquisiteBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(creature.getId());
    }

    @Test
    @DisplayName("A borrowed creature returns under its owner's control")
    void borrowedCreatureReturnsToOwner() {
        harness.addToBattlefield(player1, new OjiTheExquisiteBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(creature.getId(), player2.getId());

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isNotEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Spell counting resets for the next turn")
    void secondSpellTriggersAgainOnNextTurn() {
        harness.addToBattlefield(player1, new OjiTheExquisiteBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        var firstReturnId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThat(firstReturnId).isNotEqualTo(creature.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.handlePermanentChosen(player1, firstReturnId);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(firstReturnId);
    }

    @Test
    @DisplayName("A target that is no longer controlled by you is not flickered")
    void targetChangingControllerBecomesIllegal() {
        harness.addToBattlefield(player1, new OjiTheExquisiteBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isEqualTo(creature.getId());
    }
}
