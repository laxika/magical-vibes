package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DrownyardExplorers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnshakableTail.class, DrownyardExplorers.class, GrizzlyBears.class,
        Millstone.class})
class UnshakableTailTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and surveils one")
    void entersAndSurveils() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.enterBattlefieldAndReturn(player1, new UnshakableTail());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Surveils one at the beginning of its controller's upkeep")
    void surveilsAtUpkeep() {
        harness.enterBattlefieldAndReturn(player1, new UnshakableTail());
        resolveInitialSurveil();

        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Investigates once when one or more of its controller's creature cards are milled")
    void investigatesOncePerMillEventWithCreatureCards() {
        harness.enterBattlefieldAndReturn(player1, new UnshakableTail());
        resolveInitialSurveil();
        harness.enterBattlefieldAndReturn(player1, new Millstone());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(countClues()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Clue and two mana return it from the graveyard to its owner's hand")
    void returnsFromGraveyardBySacrificingClue() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new DrownyardExplorers());
        resolveAllTriggers();

        UnshakableTail tail = new UnshakableTail();
        harness.setGraveyard(player1, List.of(tail));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Unshakable Tail");
        harness.assertNotInGraveyard(player1, "Unshakable Tail");
        assertThat(countClues()).isZero();
    }

    private long countClues() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Clue".equals(permanent.getCard().getName()))
                .count();
    }

    private void resolveInitialSurveil() {
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }
}
