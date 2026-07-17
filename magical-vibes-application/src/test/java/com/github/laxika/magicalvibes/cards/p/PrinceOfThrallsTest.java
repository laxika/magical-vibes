package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrinceOfThrallsTest extends BaseCardTest {

    /** player1 has the Prince and Shocks player2's Grizzly Bears; returns after both resolve. */
    private void princeAndShockGrizzly() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, dyingId);
        harness.passBothPriorities(); // Shock resolves → Grizzly Bears dies → Prince trigger stacks
        harness.passBothPriorities(); // Prince trigger resolves → the opponent is offered the choice
    }

    @Test
    @DisplayName("Opponent declines to pay — the permanent is put onto the battlefield under your control")
    void decliningStealsPermanent() {
        princeAndShockGrizzly();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent pays 3 life — the permanent stays in their graveyard")
    void payingKeepsPermanent() {
        princeAndShockGrizzly();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Opponent who can't pay 3 life is stolen from without a choice")
    void cannotPayStealsAutomatically() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, dyingId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player2, 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not trigger when the controller's own permanent is put into a graveyard")
    void doesNotTriggerForOwnPermanent() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID dyingId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, dyingId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Triggers for any permanent type — a destroyed opponent artifact can be stolen")
    void stealsNoncreaturePermanent() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities(); // Naturalize resolves → artifact dies → Prince trigger stacks
        harness.passBothPriorities(); // Prince trigger resolves → the opponent is offered the choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fountain of Youth"));
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
    }
}
