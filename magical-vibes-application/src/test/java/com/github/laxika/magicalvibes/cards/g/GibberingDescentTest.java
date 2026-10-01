package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AugurOfSkulls;
import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GibberingDescent.class, AugurOfSkulls.class, BlindPhantasm.class})
class GibberingDescentTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's upkeep makes that player lose 1 life and discard a card")
    void eachPlayersUpkeepCausesLifeLossAndDiscard() {
        harness.addToBattlefield(player1, new GibberingDescent());
        harness.setHand(player1, List.of(new BlindPhantasm()));
        harness.setHand(player2, List.of(new BlindPhantasm()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Blind Phantasm");

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Blind Phantasm");
    }

    @Test
    @DisplayName("Hellbent skips only the controller's upkeep when their hand is empty")
    void hellbentSkipsOnlyControllerUpkeep() {
        harness.addToBattlefield(player1, new GibberingDescent());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Discarding Gibbering Descent offers its madness cost")
    void discardOffersMadnessCast() {
        GibberingDescent descent = discardViaAugurOfSkulls();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(descent.getId()));
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining the madness cast puts Gibbering Descent into its owner's graveyard")
    void decliningMadnessCastPutsCardIntoGraveyard() {
        GibberingDescent descent = discardViaAugurOfSkulls();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(descent.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(descent.getId()));
    }

    @Test
    @DisplayName("Accepting madness casts Gibbering Descent for {2}{B}{B}")
    void acceptingMadnessCastPaysTheMadnessCost() {
        GibberingDescent descent = discardViaAugurOfSkulls();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(descent.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private GibberingDescent discardViaAugurOfSkulls() {
        GibberingDescent descent = new GibberingDescent();
        harness.addToBattlefield(player2, new AugurOfSkulls());
        harness.setHand(player1, List.of(descent, new BlindPhantasm()));

        advanceToUpkeep(player2);
        harness.activateAbility(player2, 0, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        return descent;
    }
}
