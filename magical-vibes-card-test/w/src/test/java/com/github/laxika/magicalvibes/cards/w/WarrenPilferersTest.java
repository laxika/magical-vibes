package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
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

    /** Casts Warren Pilferers and resolves the creature spell, up to the ETB trigger's graveyard target choice. */
    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarrenPilferers()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    /** Chooses {@code target} for the ETB trigger and resolves the trigger. */
    private void chooseTargetAndResolve(Card target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();
    }

    private Permanent findPilferers() {
        return findPermanent(player1, "Warren Pilferers");
    }

    @Test
    @DisplayName("Returns chosen creature card from graveyard to hand")
    void returnsCreatureToHand() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castAndResolveEtb();

        chooseTargetAndResolve(bears);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returning a Goblin card grants Warren Pilferers haste until end of turn")
    void goblinGrantsHaste() {
        Card piker = new GoblinPiker();
        harness.setGraveyard(player1, List.of(piker));
        castAndResolveEtb();

        chooseTargetAndResolve(piker);

        harness.assertInHand(player1, "Goblin Piker");
        assertThat(findPilferers().getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Returning a non-Goblin creature does NOT grant haste")
    void nonGoblinDoesNotGrantHaste() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castAndResolveEtb();

        chooseTargetAndResolve(bears);

        assertThat(findPilferers().getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Chooses the Goblin among several creatures to gain haste")
    void choosesGoblinAmongCreatures() {
        Card piker = new GoblinPiker();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), piker));
        castAndResolveEtb();

        chooseTargetAndResolve(piker);

        harness.assertInHand(player1, "Goblin Piker");
        assertThat(findPilferers().getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Card piker = new GoblinPiker();
        harness.setGraveyard(player1, List.of(piker));
        castAndResolveEtb();
        chooseTargetAndResolve(piker);

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

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(findPilferers().getGrantedKeywords()).doesNotContain(Keyword.HASTE);
        harness.assertOnBattlefield(player1, "Warren Pilferers");
    }

    @Test
    @DisplayName("Cannot choose a non-creature card from graveyard")
    void cannotChooseNonCreature() {
        Card holyDay = new HolyDay();
        harness.setGraveyard(player1, List.of(holyDay, new GrizzlyBears()));
        castAndResolveEtb();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(holyDay.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");
    }

    @Test
    @DisplayName("The graveyard target must be chosen before the ETB ability resolves")
    void choosesTargetWhenTriggerIsPutOnStack() {
        Card piker = new GoblinPiker();
        harness.setGraveyard(player1, List.of(piker));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarrenPilferers()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(piker.getId()));
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

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Cannot return a creature from the opponent's graveyard")
    void doesNotReturnOpponentsCreature() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GoblinPiker()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Goblin Piker");
        harness.assertNotInHand(player1, "Goblin Piker");
        assertThat(findPilferers().getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }
}
