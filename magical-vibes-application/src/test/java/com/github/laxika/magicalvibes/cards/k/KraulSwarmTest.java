package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KraulSwarm.class, DeadWeight.class})
class KraulSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard to its owner's hand after discarding a creature")
    void returnsToHandAfterDiscardingCreature() {
        KraulSwarm swarm = new KraulSwarm();
        Card discardedCreature = new KraulSwarm();
        harness.setGraveyard(player1, List.of(swarm));
        harness.setHand(player1, List.of(discardedCreature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(swarm.getId()))
                .noneMatch(card -> card.getId().equals(discardedCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(swarm.getId()));
    }

    @Test
    @DisplayName("Cannot activate without a creature card to discard")
    void cannotActivateWithoutCreatureToDiscard() {
        KraulSwarm swarm = new KraulSwarm();
        Card nonCreature = new DeadWeight();
        harness.setGraveyard(player1, List.of(swarm));
        harness.setHand(player1, List.of(nonCreature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(nonCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(swarm.getId()));
    }

    @Test
    @DisplayName("Discard is paid before resolution and only the activated copy returns")
    void returnsOnlyActivatedCopy() {
        KraulSwarm source = new KraulSwarm();
        KraulSwarm otherCopy = new KraulSwarm();
        KraulSwarm discarded = new KraulSwarm();
        harness.setGraveyard(player1, List.of(source, otherCopy));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(source, otherCopy, discarded);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(otherCopy, discarded);
    }

    @Test
    @DisplayName("Cannot activate without the required black mana")
    void cannotPayBlackRequirementWithColorlessMana() {
        KraulSwarm source = new KraulSwarm();
        KraulSwarm discarded = new KraulSwarm();
        harness.setGraveyard(player1, List.of(source));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
    }

    @Test
    @DisplayName("An old activation cannot return the source after it leaves and reenters the graveyard")
    void oldActivationCannotReturnNewGraveyardObject() {
        KraulSwarm source = new KraulSwarm();
        KraulSwarm otherSource = new KraulSwarm();
        KraulSwarm firstDiscard = new KraulSwarm();
        KraulSwarm secondDiscard = new KraulSwarm();
        harness.setGraveyard(player1, List.of(source, otherSource));
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);

        harness.activateGraveyardAbility(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(otherSource));
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherSource);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
    }

    @Test
    @DisplayName("Cannot activate with less than three mana")
    void cannotActivateWithInsufficientMana() {
        KraulSwarm source = new KraulSwarm();
        KraulSwarm discarded = new KraulSwarm();
        harness.setGraveyard(player1, List.of(source));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
    }
}
