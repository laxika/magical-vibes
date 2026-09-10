package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Gravedigger.class, TrainedArmodon.class, LowlandGiant.class, Shatter.class})
class GravediggerTest extends BaseCardTest {

    private void castGravedigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Gravedigger(), "{3}{B}");
    }

    private void castAndChooseTarget(Card target) {
        castGravedigger();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void castAndAcceptMay(Card target) {
        castAndChooseTarget(target);
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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
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

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gravedigger");
    }

    @Test
    @DisplayName("Gravedigger chooses its target before asking whether to return it")
    void choosesTargetBeforeMayDecision() {
        TrainedArmodon target = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(target));
        castGravedigger();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Trained Armodon");
    }

    @Test
    @DisplayName("Accepting the may ability returns the targeted creature to hand")
    void acceptingMayReturnsTargetToHand() {
        TrainedArmodon target = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(target));
        castAndAcceptMay(target);

        harness.assertInHand(player1, "Trained Armodon");
        harness.assertNotInGraveyard(player1, "Trained Armodon");
    }

    @Test
    @DisplayName("Declining the may ability leaves the targeted creature in the graveyard")
    void decliningMaySkipsAbility() {
        TrainedArmodon target = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(target));
        castAndChooseTarget(target);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gravedigger");
        harness.assertInGraveyard(player1, "Trained Armodon");
    }

    @Test
    @DisplayName("Returns the selected creature from the graveyard to hand")
    void returnsCreatureFromGraveyardToHand() {
        TrainedArmodon target = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(target));
        castAndAcceptMay(target);

        harness.assertInHand(player1, "Trained Armodon");
        harness.assertNotInGraveyard(player1, "Trained Armodon");
        assertThat(gd.gameLog)
                .extracting(GameLogEntry::plainText)
                .contains("Alice returns Trained Armodon from graveyard to hand.");
    }

    @Test
    @DisplayName("Target selection is mandatory even though the return is optional")
    void targetSelectionCannotBeDeclined() {
        TrainedArmodon target = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(target));
        castGravedigger();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose 1 cards");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Trained Armodon");
    }

    @Test
    @DisplayName("Choosing a specific creature when multiple creatures are in the graveyard")
    void choosesSpecificCreatureFromGraveyard() {
        TrainedArmodon first = new TrainedArmodon();
        LowlandGiant second = new LowlandGiant();
        harness.setGraveyard(player1, List.of(first, second));
        castGravedigger();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Lowland Giant");
        harness.assertInGraveyard(player1, "Trained Armodon");
        harness.assertNotInGraveyard(player1, "Lowland Giant");
    }

    @Test
    @DisplayName("No trigger is put on the stack when the graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        castGravedigger();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gravedigger");
    }

    @Test
    @DisplayName("No trigger is put on the stack when the graveyard has only noncreatures")
    void noEffectWithOnlyNonCreaturesInGraveyard() {
        harness.setGraveyard(player1, List.of(new Shatter()));
        castGravedigger();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shatter");
    }

    @Test
    @DisplayName("Cannot choose a noncreature card from the graveyard")
    void cannotChooseNonCreatureFromGraveyard() {
        Shatter nonCreature = new Shatter();
        TrainedArmodon creature = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(nonCreature, creature));
        castGravedigger();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        int logSizeBefore = gd.gameLog.size();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");
        assertThat(gd.gameLog).hasSize(logSizeBefore);

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("A creature in the opponent's graveyard is not a legal target")
    void onlyTargetsOwnGraveyard() {
        TrainedArmodon target = new TrainedArmodon();
        harness.setGraveyard(player2, List.of(target));
        castGravedigger();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Trained Armodon");
    }

    @Test
    @DisplayName("Opponent cannot choose the controller's graveyard target")
    void opponentCannotChoose() {
        TrainedArmodon target = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(target));
        castGravedigger();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterFullResolution() {
        TrainedArmodon target = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(target));
        castAndAcceptMay(target);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gravedigger remains on the battlefield after returning a creature")
    void gravediggerRemainsOnBattlefield() {
        TrainedArmodon target = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(target));
        castAndAcceptMay(target);

        harness.assertOnBattlefield(player1, "Gravedigger");
    }
}
