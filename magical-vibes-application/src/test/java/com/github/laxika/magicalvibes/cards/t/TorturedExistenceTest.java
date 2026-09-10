package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorturedExistence.class, SpinedWurm.class, Shock.class})
class TorturedExistenceTest extends BaseCardTest {

    @Test
    @DisplayName("Activation only offers creature cards for the discard cost")
    void activationOffersOnlyCreatureCardsForDiscard() {
        setUpMain();
        harness.addToBattlefield(player1, new TorturedExistence());
        Card target = new SpinedWurm();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Shock(), new SpinedWurm()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Discarding a creature returns a targeted creature card from the graveyard to hand")
    void discardCreatureReturnsTargetedCreature() {
        setUpMain();
        harness.addToBattlefield(player1, new TorturedExistence());
        Card discardedCreature = new SpinedWurm();
        Card returnedCreature = new SpinedWurm();
        harness.setHand(player1, List.of(discardedCreature));
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, returnedCreature.getId(), Zone.GRAVEYARD);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(discardedCreature.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(returnedCreature.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        setUpMain();
        harness.addToBattlefield(player1, new TorturedExistence());
        Card discardedCreature = new SpinedWurm();
        Card noncreature = new Shock();
        harness.setHand(player1, List.of(discardedCreature));
        harness.setGraveyard(player1, List.of(noncreature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardedCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
    }

    @Test
    @DisplayName("Activation requires a creature card to discard")
    void activationRequiresCreatureToDiscard() {
        setUpMain();
        harness.addToBattlefield(player1, new TorturedExistence());
        Card noncreature = new Shock();
        Card target = new SpinedWurm();
        harness.setHand(player1, List.of(noncreature));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Activation requires black mana")
    void activationRequiresBlackMana() {
        setUpMain();
        harness.addToBattlefield(player1, new TorturedExistence());
        Card discardedCreature = new SpinedWurm();
        Card target = new SpinedWurm();
        harness.setHand(player1, List.of(discardedCreature));
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardedCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        setUpMain();
        harness.addToBattlefield(player1, new TorturedExistence());
        Card discardedCreature = new SpinedWurm();
        Card opponentsCreature = new SpinedWurm();
        harness.setHand(player1, List.of(discardedCreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                opponentsCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardedCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCreature);
    }

    private void setUpMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
