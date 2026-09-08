package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RaiseDead;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gravedigger.class, GrizzlyBears.class, HillGiant.class, RaiseDead.class})
class GravediggerTest extends BaseCardTest {

    /** Casts Gravedigger and leaves its creature spell on the stack. */
    private void castGravedigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Gravedigger(), "{3}{B}");
    }

    private void chooseTarget(int validTargetIndex) {
        castGravedigger();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).hasSizeGreaterThan(validTargetIndex);

        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().get(validTargetIndex)));
        harness.passBothPriorities();
    }

    private void castAndAcceptMay() {
        castAndAcceptMay(0);
    }

    private void castAndAcceptMay(int validTargetIndex) {
        chooseTarget(validTargetIndex);
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Casting Gravedigger puts it on the stack")
    void castingPutsOnStack() {
        castGravedigger();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Cannot cast Gravedigger without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving puts Gravedigger on the battlefield")
    void resolvingPutsOnBattlefield() {
        castGravedigger();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gravedigger");
    }

    @Test
    @DisplayName("Resolving Gravedigger prompts for the graveyard target before the may choice")
    void resolvingPromptsForGraveyardTarget() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castGravedigger();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability returns the selected creature to hand")
    void acceptingMayReturnsSelectedCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        chooseTarget(0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may ability does not put anything on the stack")
    void decliningMaySkipsAbility() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        chooseTarget(0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gravedigger");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a creature from the graveyard to hand")
    void returnsCreatureFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.gameLog)
                .extracting(GameLogEntry::plainText)
                .contains("Alice returns Grizzly Bears from graveyard to hand.");
    }

    @Test
    @DisplayName("Player can decline the may ability after choosing a target")
    void playerCanDeclineMayAbility() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        chooseTarget(0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing a specific creature when multiple are in the graveyard")
    void choosesSpecificCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant()));
        castAndAcceptMay(1);

        harness.assertInHand(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("ETB is not put on the stack if the graveyard is empty")
    void noTriggerWithEmptyGraveyard() {
        castGravedigger();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gravedigger");
    }

    @Test
    @DisplayName("ETB is not put on the stack if the graveyard has no creature cards")
    void noTriggerWithOnlyNonCreaturesInGraveyard() {
        harness.setGraveyard(player1, List.of(new RaiseDead()));
        castGravedigger();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Raise Dead");
    }

    @Test
    @DisplayName("Cannot choose a non-creature card as the graveyard target")
    void cannotChooseNonCreatureFromGraveyard() {
        RaiseDead nonCreature = new RaiseDead();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonCreature, creature));
        castGravedigger();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        int logSizeBefore = gd.gameLog.size();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");
        assertThat(gd.gameLog).hasSize(logSizeBefore);
    }

    @Test
    @DisplayName("Opponent cannot choose the controller's graveyard target")
    void opponentCannotChoose() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castGravedigger();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(choice.validCardIds().getFirst())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterFullResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gravedigger remains on the battlefield after returning a creature")
    void gravediggerRemainsOnBattlefield() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        harness.assertOnBattlefield(player1, "Gravedigger");
    }
}
