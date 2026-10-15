package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.t.TrueBeliever;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SheoldredWhisperingOne.class, GrizzlyBears.class, AngelOfMercy.class, GiantSpider.class, HolyDay.class, TrueBeliever.class, Swamp.class})
class SheoldredWhisperingOneTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's upkeep returns creature from graveyard to battlefield")
    void upkeepReturnsCreatureFromGraveyard() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns specific creature when multiple are in graveyard")
    void returnsSpecificCreatureFromGraveyard() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new AngelOfMercy()));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).get(1).getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angel of Mercy");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Angel of Mercy");
    }

    @Test
    @DisplayName("No effect when graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No effect when graveyard has only non-creature cards")
    void noEffectWithOnlyNonCreaturesInGraveyard() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.setGraveyard(player1, List.of(new HolyDay()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("Upkeep trigger does NOT fire during opponent's upkeep")
    void upkeepTriggerDoesNotFireDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent with one creature sacrifices it on their upkeep")
    void opponentSacrificesSingleCreature() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent with multiple creatures is prompted to choose which to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve sacrifice trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        // Player 2 chooses to sacrifice Grizzly Bears
        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("No sacrifice effect when opponent has no creatures")
    void noSacrificeWhenOpponentHasNoCreatures() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no creatures to sacrifice"));
    }

    @Test
    @DisplayName("Sacrifice trigger does NOT fire during controller's own upkeep")
    void sacrificeTriggerDoesNotFireDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returned creature's ETB ability triggers")
    void returnedCreatureTriggersETB() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.passBothPriorities();

        // Angel of Mercy's ETB (gain 3 life) should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angel of Mercy");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Losing the chosen graveyard target does not allow choosing another creature")
    void doesNotRetargetAfterTargetLeavesGraveyard() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        GrizzlyBears target = new GrizzlyBears();
        AngelOfMercy other = new AngelOfMercy();
        harness.setGraveyard(player1, List.of(target, other));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angel of Mercy");
        harness.assertInGraveyard(player1, "Angel of Mercy");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent with shroud still sacrifices a creature")
    void sacrificeDoesNotTargetOpponent() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        harness.addToBattlefield(player2, new TrueBeliever());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "True Believer");
        harness.assertInGraveyard(player2, "True Believer");
    }

    @Test
    @DisplayName("Only creature cards in the controller's graveyard can be targeted")
    void targetChoiceExcludesNoncreaturesAndOpponentsGraveyard() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new HolyDay(), target));
        harness.setGraveyard(player2, List.of(new AngelOfMercy()));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Day");
        harness.assertInGraveyard(player2, "Angel of Mercy");
    }

    @Test
    @DisplayName("The controller must select a graveyard target when one is available")
    void cannotDeclineTargetSelection() {
        harness.addToBattlefield(player1, new SheoldredWhisperingOne());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Swampwalk prevents blocking while the defender controls a Swamp")
    void swampwalkPreventsBlocking() {
        addCreatureReady(player1, new SheoldredWhisperingOne());
        harness.addToBattlefield(player2, new Swamp());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Swamp controlled only by the attacker does not prevent blocking")
    void swampwalkRequiresDefendersSwamp() {
        addCreatureReady(player1, new SheoldredWhisperingOne());
        harness.addToBattlefield(player1, new Swamp());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
