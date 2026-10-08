package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarrenPilferers.class, GoblinPiker.class, GrizzlyBears.class, HolyDay.class})
class WarrenPilferersTest extends BaseCardTest {

    /** Casts Warren Pilferers and resolves it plus its ETB trigger, up to the graveyard choice. */
    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarrenPilferers()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private Permanent findPilferers() {
        return findPermanent(player1, "Warren Pilferers");
    }

    @Test
    @DisplayName("Returns chosen creature card from graveyard to hand")
    void returnsCreatureToHand() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returning a Goblin card grants Warren Pilferers haste until end of turn")
    void goblinGrantsHaste() {
        harness.setGraveyard(player1, List.of(new GoblinPiker()));
        castAndResolveEtb();

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Goblin Piker");
        assertThat(findPilferers().getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Returning a non-Goblin creature does NOT grant haste")
    void nonGoblinDoesNotGrantHaste() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndResolveEtb();

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPilferers().getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Chooses the Goblin among several creatures to gain haste")
    void choosesGoblinAmongCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GoblinPiker()));
        castAndResolveEtb();

        harness.handleGraveyardCardChosen(player1, 1); // Goblin Piker

        harness.assertInHand(player1, "Goblin Piker");
        assertThat(findPilferers().getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new GoblinPiker()));
        castAndResolveEtb();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPilferers().getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPilferers().getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("No creatures in graveyard: no choice, no haste, creature still enters")
    void noCreaturesInGraveyard() {
        harness.setGraveyard(player1, List.of(new HolyDay()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(findPilferers().getGrantedKeywords()).doesNotContain(Keyword.HASTE);
        harness.assertOnBattlefield(player1, "Warren Pilferers");
    }

    @Test
    @DisplayName("Cannot choose a non-creature card from graveyard")
    void cannotChooseNonCreature() {
        harness.setGraveyard(player1, List.of(new HolyDay(), new GrizzlyBears()));
        castAndResolveEtb();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0)) // HolyDay is not a creature
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("The graveyard target must be chosen before the ETB ability resolves")
    void choosesTargetWhenTriggerIsPutOnStack() {
        harness.setGraveyard(player1, List.of(new GoblinPiker()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarrenPilferers()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Goblin Piker");
        harness.assertNotInHand(player1, "Goblin Piker");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Goblin Piker");
        assertThat(findPilferers().getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Returning a creature is mandatory when a legal creature card is available")
    void cannotDeclineCreatureReturn() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndResolveEtb();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return a creature from the opponent's graveyard")
    void doesNotReturnOpponentsCreature() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GoblinPiker()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Goblin Piker");
        harness.assertNotInHand(player1, "Goblin Piker");
        assertThat(findPilferers().getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }
}
