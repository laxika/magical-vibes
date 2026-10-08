package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.ManaLeak;
import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CalculatedDismissal.class, GrizzlyBears.class, LightningBolt.class,
        LlanowarElves.class, ManaLeak.class, ActOfTreason.class})
class CalculatedDismissalTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void keepPriorityForResponses() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.EnumSet.of(
                com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN,
                com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.EnumSet.of(
                com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN,
                com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE));
    }


    @Test
    @DisplayName("Counters the target spell when its controller cannot pay {3}")
    void countersWhenControllerCannotPay() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new CalculatedDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Target spell survives when its controller pays {3}")
    void spellSurvivesWhenControllerPays() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 4); // 1 to cast, 3 to pay

        harness.setHand(player2, List.of(new CalculatedDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Spell mastery is off with fewer than two instant/sorcery cards in the graveyard")
    void noScryWithoutSpellMastery() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new CalculatedDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setGraveyard(player2, List.of(new LightningBolt()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spell mastery scries 2 with two instant/sorcery cards in the graveyard")
    void scriesTwoWithSpellMastery() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new CalculatedDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setGraveyard(player2, List.of(new LightningBolt(), new ManaLeak()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).hasSize(2);
    }

    @Test
    @DisplayName("The payment decision precedes scry, and paying does not prevent scry")
    void paymentPrecedesScry() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new CalculatedDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setGraveyard(player2, List.of(new CalculatedDismissal(), new ActOfTreason()));
        harness.setLibrary(player2, List.of(new CalculatedDismissal(), new ActOfTreason()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.playerDecks.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Act of Treason", "Calculated Dismissal");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Declining payment counters the spell before scry")
    void decliningPaymentCountersBeforeScry() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new CalculatedDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setGraveyard(player2, List.of(new ActOfTreason(), new ActOfTreason()));
        harness.setLibrary(player2, List.of(new CalculatedDismissal(), new ActOfTreason()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Countering your own instant supplies the second card for spell mastery")
    void counteredOwnInstantEnablesSpellMastery() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt, new CalculatedDismissal()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setGraveyard(player1, List.of(new ActOfTreason()));
        harness.setLibrary(player1, List.of(new CalculatedDismissal(), new ActOfTreason()));

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, bolt.getId());

        harness.assertInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent graveyard cards and creature cards do not enable spell mastery")
    void ignoresOpponentGraveyardAndCreatureCards() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setGraveyard(player1, List.of(new CalculatedDismissal(), new ActOfTreason()));
        harness.setHand(player2, List.of(new CalculatedDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setGraveyard(player2, List.of(new ActOfTreason(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An illegal target prevents both the counter and the scry")
    void doesNotScryWhenTargetLeavesStack() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt, new ManaLeak()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new CalculatedDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setGraveyard(player2, List.of(new CalculatedDismissal(), new ActOfTreason()));
        harness.setLibrary(player2, List.of(new CalculatedDismissal(), new ActOfTreason()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bolt.getId());
        harness.castAndResolveInstant(player1, 0, bolt.getId());
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Calculated Dismissal");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        harness.assertLife(player2, 20);
    }
}
