package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongcrafterMage.class, Divination.class, GrizzlyBears.class, Shock.class})
class SongcrafterMageTest extends BaseCardTest {

    @Test
    void grantsHarmonizeToTargetInstantOrSorceryAndAllowsCastingIt() {
        SongcrafterMage mage = new SongcrafterMage();
        Card shock = new Shock();
        harness.setHand(player1, List.of(mage));
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        assertThat(gd.cardsGrantedHarmonizeUntilEndOfTurn).contains(shock.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void harmonizeUsesTheTargetCardManaCostAndCreaturePowerReduction() {
        SongcrafterMage mage = new SongcrafterMage();
        Divination divination = new Divination();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        Card drawnCard = new Shock();
        harness.setLibrary(player1, List.of(drawnCard, new Shock()));
        harness.setHand(player1, List.of(mage));
        harness.setGraveyard(player1, List.of(divination));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();
        assertThat(gd.cardsGrantedHarmonizeUntilEndOfTurn).contains(divination.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(divination);
    }

    @Test
    void cannotTargetNonInstantOrSorceryCardInGraveyard() {
        SongcrafterMage mage = new SongcrafterMage();
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(mage));
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
