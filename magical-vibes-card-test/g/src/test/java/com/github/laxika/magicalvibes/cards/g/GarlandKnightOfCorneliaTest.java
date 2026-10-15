package com.github.laxika.magicalvibes.cards.g;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.c.ChaosTheEndless;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({GarlandKnightOfCornelia.class, ChaosTheEndless.class, DarkRitual.class,
        Forest.class, GrizzlyBears.class})
class GarlandKnightOfCorneliaTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 when its controller casts a noncreature spell")
    void castsNoncreatureSpellSurveilsOne() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new GarlandKnightOfCornelia());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.MayAbilityChoice surveil =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(surveil).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Does not surveil when its controller casts a creature spell")
    void castsCreatureSpellDoesNotSurveil() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new GarlandKnightOfCornelia());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Returns from the graveyard transformed as Chaos, the Endless")
    void returnsFromGraveyardTransformed() {
        GarlandKnightOfCornelia card = new GarlandKnightOfCornelia();
        harness.setGraveyard(player1, List.of(card));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addGarlandMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent chaos = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(chaos.isTransformed()).isTrue();
        assertThat(chaos.getCard()).isInstanceOf(ChaosTheEndless.class);
        harness.assertNotInGraveyard(player1, "Garland, Knight of Cornelia");
    }

    @Test
    @DisplayName("Puts Chaos on the bottom of its owner's library when it dies")
    void putsChaosOnOwnersLibraryBottomWhenItDies() {
        GarlandKnightOfCornelia card = new GarlandKnightOfCornelia();
        Permanent chaos = new Permanent(card);
        chaos.setCard(card.getBackFaceCard());
        chaos.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(chaos);
        Card existingLibraryCard = new Forest();
        harness.setLibrary(player1, List.of(existingLibraryCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, chaos));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(existingLibraryCard, card);
    }

    @Test
    void canLeaveSurveilledCardOnTop() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new GarlandKnightOfCornelia());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerSurveil() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new GarlandKnightOfCornelia());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void surveilWithEmptyLibraryDoesNotRequireChoice() {
        harness.setLibrary(player1, List.of());
        addCreatureReady(player1, new GarlandKnightOfCornelia());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateGraveyardAbilityOutsideMainPhase() {
        harness.setGraveyard(player1, List.of(new GarlandKnightOfCornelia()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        addGarlandMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Garland, Knight of Cornelia");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateGraveyardAbilityOnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new GarlandKnightOfCornelia()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
        addGarlandMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Garland, Knight of Cornelia");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateGraveyardAbilityWhileSpellIsOnStack() {
        harness.setGraveyard(player1, List.of(new GarlandKnightOfCornelia()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        addGarlandMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Garland, Knight of Cornelia");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void frontFaceStaysInGraveyardWhenItDies() {
        GarlandKnightOfCornelia card = new GarlandKnightOfCornelia();
        Permanent garland = addCreatureReady(player1, card);
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, garland));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void chaosDyingUnderOpponentsControlReturnsToOwnersLibrary() {
        GarlandKnightOfCornelia card = new GarlandKnightOfCornelia();
        card.setOwnerId(player1.getId());
        Permanent chaos = new Permanent(card);
        chaos.setCard(card.getBackFaceCard());
        chaos.setTransformed(true);
        gd.playerBattlefields.get(player2.getId()).add(chaos);
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLibrary(player2, List.of());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, chaos));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, card);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void addGarlandMana() {
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
