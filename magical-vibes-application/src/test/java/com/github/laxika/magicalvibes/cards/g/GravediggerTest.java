package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gravedigger.class, GrizzlyBears.class, AirElemental.class, Disenchant.class})
class GravediggerTest extends BaseCardTest {

    /**
     * Casts Gravedigger, chooses its ETB target, and accepts the may ability so the return
     * resolves inline.
     */
    private void castAndAcceptMay() {
        castAndAcceptMay(0);
    }

    private void castAndAcceptMay(int targetIndex) {
        castAndChooseTarget(targetIndex);
        harness.passBothPriorities(); // resolve the triggered ability
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true); // accept - return the targeted card
    }

    private void castAndChooseTarget(int targetIndex) {
        castGravedigger();
        harness.passBothPriorities(); // resolve creature spell; target is chosen for the ETB

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).hasSizeGreaterThan(targetIndex);
        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().get(targetIndex)));
    }

    private void castGravedigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Gravedigger(), "{3}{B}");
    }

    // ===== Casting =====

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

    // ===== ETB may ability =====

    @Test
    @DisplayName("Resolving Gravedigger prompts for its target before the may choice")
    void resolvingPromptsForTargetBeforeMayChoice() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castGravedigger();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
    }

    @Test
    @DisplayName("Accepting may ability returns the previously targeted creature")
    void acceptingMayResolvesEtbInline() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndChooseTarget(0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining may ability does not put anything on the stack")
    void decliningMaySkipsAbility() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castAndChooseTarget(0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        // Gravedigger still on battlefield
        harness.assertOnBattlefield(player1, "Gravedigger");
        // Grizzly Bears still in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    // ===== Graveyard return to hand =====

    @Test
    @DisplayName("Returns creature from graveyard to hand")
    void returnsCreatureFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        // Grizzly Bears moved from graveyard to hand
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.gameLog)
                .extracting(GameLogEntry::plainText)
                .contains("Alice returns Grizzly Bears from graveyard to hand.");
    }

    @Test
    @DisplayName("Target choice cannot be declined")
    void targetChoiceCannotBeDeclined() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        castGravedigger();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isSameAs(choice);
    }

    @Test
    @DisplayName("Choosing specific creature when multiple are in graveyard")
    void choosesSpecificCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new AirElemental()));
        castAndAcceptMay(1);

        // Air Elemental returned to hand, Grizzly Bears stays in graveyard
        harness.assertInHand(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Air Elemental");
    }

    // ===== Empty / no creatures in graveyard =====

    @Test
    @DisplayName("ETB resolves with no effect if graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        castGravedigger();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gravedigger");
    }

    @Test
    @DisplayName("ETB resolves with no effect if graveyard has only non-creature cards")
    void noEffectWithOnlyNonCreaturesInGraveyard() {
        harness.setGraveyard(player1, List.of(new Disenchant()));
        castGravedigger();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        // Disenchant stays in graveyard untouched
        harness.assertInGraveyard(player1, "Disenchant");
    }

    // ===== Invalid choices =====

    @Test
    @DisplayName("Cannot choose non-creature card from graveyard")
    void cannotChooseNonCreatureFromGraveyard() {
        Card nonCreature = new Disenchant();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonCreature, creature));
        castGravedigger();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isSameAs(choice);
    }

    @Test
    @DisplayName("Opponent cannot make graveyard choice for controller")
    void opponentCannotChoose() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        castGravedigger();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    // ===== Stack is empty after full resolution =====

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterFullResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gravedigger remains on battlefield after returning a creature")
    void gravediggerRemainsOnBattlefield() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        harness.assertOnBattlefield(player1, "Gravedigger");
    }
}

