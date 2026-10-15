package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FandanielTelophoroiAscian.class, GrizzlyBears.class, Shock.class, CounselOfTheSoratami.class})
class FandanielTelophoroiAscianTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils when you cast an instant or sorcery")
    void surveilsOnInstantOrSorceryCast() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent may sacrifice a nontoken creature")
    void opponentMaySacrificeNontokenCreature() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent who declines loses life for each instant or sorcery in your graveyard")
    void opponentDeclinesAndLosesScaledLife() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void surveilCanKeepTopCardBeforeSorceryResolves() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard).hasSize(2);
    }

    @Test
    void creatureSpellDoesNotTriggerSurveil() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    void opponentInstantDoesNotTriggerSurveil() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 18);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void countsBothSpellTypesButOnlyInControllersGraveyard() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.setGraveyard(player1, List.of(new Shock(), new CounselOfTheSoratami(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock(), new CounselOfTheSoratami()));

        advanceToEndStepAndResolve();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void tokenCreatureCannotBeSacrificedInsteadOfLosingLife() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        harness.addToBattlefield(player2, token);
        harness.setGraveyard(player1, List.of(new Shock()));

        advanceToEndStepAndResolve();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyGraveyardStillAllowsSacrificeButDecliningCostsNoLife() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void surveilWithEmptyLibraryStillLetsInstantResolve() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void opponentChoosesWhichNontokenCreatureToSacrifice() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        var first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(second.getCard());
        harness.assertLife(player2, 20);
    }

    @Test
    void lifeLossUsesGraveyardAtResolutionRatherThanTriggerTime() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.setGraveyard(player1, List.of(new Shock(), new CounselOfTheSoratami()));

        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void opponentsEndStepDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void resolveEndStepTrigger() {
        advanceToEndStepAndResolve();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    private void advanceToEndStepAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
