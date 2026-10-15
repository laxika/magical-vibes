package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Paleoloth.class, CrawWurm.class, GrizzlyBears.class, Shock.class})
class PaleolothTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting returns a creature card from graveyard to hand when a power-5+ creature enters")
    void triggersAndReturnsCreatureWhenBigCreatureEnters() {
        harness.addToBattlefield(player1, new Paleoloth());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        // Craw Wurm (6/4) — power 6 >= 5 triggers Paleoloth
        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities(); // Resolve Craw Wurm → trigger queued

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the creature card in the graveyard")
    void decliningLeavesCreatureInGraveyard() {
        harness.addToBattlefield(player1, new Paleoloth());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities(); // Resolve Craw Wurm → trigger queued
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false); // Decline

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("No ability remains on the stack when the graveyard contains only a noncreature")
    void nonCreatureCardsAreNotReturnable() {
        harness.addToBattlefield(player1, new Paleoloth());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Does not trigger when a creature with power below 5 enters")
    void doesNotTriggerForLowPowerCreature() {
        harness.addToBattlefield(player1, new Paleoloth());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        // Grizzly Bears (2/2) — power 2 does not trigger
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities(); // Resolve Grizzly Bears

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not trigger for itself entering the battlefield")
    void doesNotTriggerForItself() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        // Paleoloth (5/5) entering must not trigger its own "another creature" ability
        harness.castFromHand(player1, new Paleoloth(), "{4}{G}{G}");
        harness.passBothPriorities(); // Resolve Paleoloth

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's power-5+ creature enters")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new Paleoloth());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Opponent's Craw Wurm (6/4) enters — must not trigger Paleoloth
        harness.castFromHand(player2, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities(); // Resolve Craw Wurm

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Another Paleoloth at exactly five power triggers the existing Paleoloth")
    void triggersAtExactlyFivePower() {
        harness.addToBattlefield(player1, new Paleoloth());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.castFromHand(player1, new Paleoloth(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability cannot retarget when its chosen creature leaves the graveyard")
    void doesNotRetargetAtResolution() {
        harness.addToBattlefield(player1, new Paleoloth());
        GrizzlyBears target = new GrizzlyBears();
        CrawWurm other = new CrawWurm();
        harness.setGraveyard(player1, List.of(target, other));
        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Craw Wurm");
        harness.assertNotInHand(player1, "Craw Wurm");
    }

    @Test
    @DisplayName("An empty graveyard provides no legal target and no optional return prompt")
    void emptyGraveyardDoesNotLeaveAbilityOnStack() {
        harness.addToBattlefield(player1, new Paleoloth());
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
