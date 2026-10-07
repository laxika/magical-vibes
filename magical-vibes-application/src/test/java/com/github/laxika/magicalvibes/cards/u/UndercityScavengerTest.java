package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CatacombCrocodile;
import com.github.laxika.magicalvibes.cards.g.GrotesqueDemise;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndercityScavenger.class, GrizzlyBears.class, CatacombCrocodile.class, GrotesqueDemise.class})
class UndercityScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts two counters on Undercity Scavenger and scries 2")
    void sacrificesAnotherCreatureAndScriesTwo() {
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        var scavenger = findPermanent(player1, "Undercity Scavenger");
        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the sacrifice does not put counters on Undercity Scavenger or scry")
    void decliningSacrificeDoesNothing() {
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Undercity Scavenger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
    }

    @Test
    @DisplayName("With no other creature, accepting the sacrifice has no effect")
    void noOtherCreatureMeansNoEffect() {
        prepareCast(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Undercity Scavenger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("The sacrifice choice excludes the source and opposing creatures")
    void onlyAnotherControlledCreatureCanBeSacrificed() {
        var ownCreature = harness.addToBattlefieldAndReturn(player1, new CatacombCrocodile());
        var opponentCreature = harness.addToBattlefieldAndReturn(player2, new CatacombCrocodile());
        prepareCast(List.of(new CatacombCrocodile()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(ownCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInGraveyard(player1, "Catacomb Crocodile");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(findPermanent(player1, "Undercity Scavenger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Scry still happens if Undercity Scavenger leaves before its trigger resolves")
    void sourceLeavingDoesNotPreventSacrificeOrScry() {
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new CatacombCrocodile());
        var first = new CatacombCrocodile();
        var second = new CatacombCrocodile();
        var third = new CatacombCrocodile();
        prepareCast(List.of(first, second, third));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        var scavenger = findPermanent(player1, "Undercity Scavenger");

        harness.setHand(player2, List.of(new GrotesqueDemise()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, scavenger.getId());
        harness.assertNotOnBattlefield(player1, "Undercity Scavenger");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        var scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));
        harness.assertInGraveyard(player1, "Catacomb Crocodile");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent the sacrifice or counters")
    void emptyLibraryStillAllowsSacrificeAndCounters() {
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new CatacombCrocodile());
        prepareCast(List.of());
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        harness.assertInGraveyard(player1, "Catacomb Crocodile");
        assertThat(findPermanent(player1, "Undercity Scavenger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void prepareCast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new UndercityScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
