package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimaryResearch.class, AvatarOfMight.class, Forest.class, GrizzlyBears.class,
        HolyDay.class, LoxodonWarhammer.class})
class PrimaryResearchTest extends BaseCardTest {

    private void castAndResolveSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PrimaryResearch()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment spell → ETB on stack
    }

    @Test
    @DisplayName("The graveyard target is chosen before the ETB ability can resolve")
    void choosesTargetWhenTriggerGoesOnStack() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castAndResolveSpell();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a nonland permanent card with mana value 3 or less to the battlefield")
    void reanimatesLowManaValuePermanent() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears())); // 2/2 creature, MV 2
        castAndResolveSpell();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A permanent card with mana value greater than 3 is not a valid choice")
    void cannotReanimateHighManaValue() {
        harness.setGraveyard(player1, List.of(new AvatarOfMight())); // MV 8
        castAndResolveSpell();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Avatar of Might");
    }

    @Test
    @DisplayName("A land card is not a valid choice")
    void cannotReanimateLand() {
        harness.setGraveyard(player1, List.of(new Forest()));
        castAndResolveSpell();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Reanimating a card triggers the end-step draw (a card left your graveyard)")
    void reanimationTriggersEndStepDraw() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        castAndResolveSpell();

        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities(); // Grizzly Bears leaves the graveyard

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP → trigger fires
        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("No end-step draw when nothing left your graveyard this turn")
    void noEndStepDrawWithoutGraveyardExit() {
        harness.addToBattlefield(player1, new PrimaryResearch());
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP; no trigger should fire

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Cannot choose a high-mana-value card even when it sits in the graveyard alongside a legal one")
    void onlyLowManaValueCardsAreValidChoices() {
        harness.setGraveyard(player1, List.of(new AvatarOfMight(), new GrizzlyBears()));
        castAndResolveSpell();
        // Index 0 is Avatar of Might (MV 8) — not a valid choice
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An instant card cannot be returned")
    void cannotReanimateNonPermanent() {
        harness.setGraveyard(player1, List.of(new HolyDay()));
        castAndResolveSpell();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("A noncreature permanent at mana value three can be returned")
    void reanimatesArtifactAtManaValueLimit() {
        harness.setGraveyard(player1, List.of(new LoxodonWarhammer()));
        castAndResolveSpell();

        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Loxodon Warhammer");
        harness.assertNotInGraveyard(player1, "Loxodon Warhammer");
    }

    @Test
    @DisplayName("A legal graveyard target must be chosen rather than declining the return")
    void cannotDeclineMandatoryTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndResolveSpell();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB cannot target a card in an opponent's graveyard")
    void cannotReanimateOpponentsCard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        castAndResolveSpell();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Reanimation does not draw a card during an opponent's end step")
    void noDrawOnOpponentsEndStep() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        castAndResolveSpell();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
