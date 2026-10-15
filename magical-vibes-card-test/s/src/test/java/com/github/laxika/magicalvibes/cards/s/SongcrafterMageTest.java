package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturesRhythm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SongcrafterMage.class, Divination.class, GrizzlyBears.class, Shock.class, NaturesRhythm.class})
class SongcrafterMageTest extends BaseCardTest {

    @Test
    void grantsHarmonizeToTargetInstantOrSorceryAndAllowsCastingIt() {
        SongcrafterMage mage = new SongcrafterMage();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.castFromHand(player1, mage, "{G}{U}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        assertThat(gd.cardsGrantedHarmonizeUntilEndOfTurn).contains(shock.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFlashback(player1, 0, player2.getId());

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
        harness.setGraveyard(player1, List.of(divination));
        harness.castFromHand(player1, mage, "{G}{U}{R}");
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
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, mage, "{G}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @CardUsed({SongcrafterMage.class, NaturesRhythm.class})
    void grantsAnAdditionalHarmonizeCostToACardThatAlreadyHasHarmonize() {
        NaturesRhythm spell = new NaturesRhythm();
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new SongcrafterMage()));
        harness.castFromHand(player1, new SongcrafterMage(), "{G}{U}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFlashback(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotInGraveyard(player1, "Nature's Rhythm");
    }

    @Test
    @CardUsed({SongcrafterMage.class, Divination.class})
    void canTapTheNewlyEnteredMageAndCannotReduceColoredMana() {
        Divination spell = new Divination();
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new SongcrafterMage(), new SongcrafterMage()));
        harness.castFromHand(player1, new SongcrafterMage(), "{G}{U}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        Permanent mage = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(mage.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mage.isTapped()).isFalse();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFlashbackWithTapCost(player1, 0, List.of(mage.getId()));
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @CardUsed({SongcrafterMage.class, Shock.class})
    void grantedHarmonizeExpiresAtEndOfTurn() {
        Shock spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));
        harness.castFromHand(player1, new SongcrafterMage(), "{G}{U}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({SongcrafterMage.class, Shock.class})
    void cannotTargetACardInTheOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.castFromHand(player1, new SongcrafterMage(), "{G}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Shock");
    }
}
