package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveVenerations.class, GrizzlyBears.class, Shock.class})
class GraveVenerationsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller the monarch")
    void enteringBattlefieldMakesControllerMonarch() {
        harness.setHand(player1, List.of(new GraveVenerations()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A creature you control dying drains each opponent and gains you 1 life")
    void ownCreatureDeathDrainsOpponentsAndGainsLife() {
        harness.addToBattlefield(player1, new GraveVenerations());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        killWithShock(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new GraveVenerations());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        killWithShock(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("At your end step while monarch, returns up to one target creature card to hand")
    void monarchEndStepReturnsCreatureFromGraveyard() {
        harness.addToBattlefield(player1, new GraveVenerations());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        gd.monarchPlayerId = player1.getId();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The end-step return does not trigger when its controller is not monarch")
    void endStepReturnRequiresMonarch() {
        harness.addToBattlefield(player1, new GraveVenerations());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.monarchPlayerId = player2.getId();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Up to one allows declining the graveyard target")
    void endStepReturnCanBeDeclined() {
        harness.addToBattlefield(player1, new GraveVenerations());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        gd.monarchPlayerId = player1.getId();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The monarch condition is checked again when the return ability resolves")
    void losingMonarchBeforeResolutionPreventsReturn() {
        harness.addToBattlefield(player1, new GraveVenerations());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        gd.monarchPlayerId = player1.getId();

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        gd.monarchPlayerId = player2.getId();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only creature cards in your own graveyard can be selected")
    void returnChoiceExcludesNoncreaturesAndOpponentsCards() {
        harness.addToBattlefield(player1, new GraveVenerations());
        GrizzlyBears ownBears = new GrizzlyBears();
        Shock shock = new Shock();
        GrizzlyBears opposingBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownBears, shock));
        harness.setGraveyard(player2, List.of(opposingBears));
        gd.monarchPlayerId = player1.getId();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(ownBears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownBears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Being monarch does not return a creature during an opponent's end step")
    void opponentsEndStepDoesNotReturnCreature() {
        harness.addToBattlefield(player1, new GraveVenerations());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.monarchPlayerId = player1.getId();

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty graveyard requires no target choice")
    void emptyGraveyardDoesNotRequireTarget() {
        harness.addToBattlefield(player1, new GraveVenerations());
        harness.setGraveyard(player1, List.of());
        gd.monarchPlayerId = player1.getId();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A selected creature that leaves the graveyard is not returned")
    void targetLeavingGraveyardPreventsReturn() {
        harness.addToBattlefield(player1, new GraveVenerations());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        gd.monarchPlayerId = player1.getId();

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    private void killWithShock(Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
