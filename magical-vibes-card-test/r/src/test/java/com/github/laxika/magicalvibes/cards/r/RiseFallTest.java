package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.b.BreedingPool;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiseFall.class, AzoriusFirstWing.class, BreedingPool.class})
class RiseFallTest extends BaseCardTest {

    @Test
    void riseReturnsTheTargetGraveyardCreatureAndBattlefieldCreatureToTheirOwnersHands() {
        Card graveyardCreature = new AzoriusFirstWing();
        harness.setGraveyard(player2, List.of(graveyardCreature));
        Permanent battlefieldCreature = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());

        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalSorcery(player1, 0, 0,
                List.of(graveyardCreature.getId(), battlefieldCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCreature.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(battlefieldCreature.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(graveyardCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(battlefieldCreature.getId()));
    }

    @Test
    void riseCannotTargetANonCreaturePermanent() {
        Permanent nonCreaturePermanent = harness.addToBattlefieldAndReturn(player2, new BreedingPool());
        Card graveyardCreature = new AzoriusFirstWing();
        harness.setGraveyard(player2, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(
                player1, 0, 0, List.of(graveyardCreature.getId(), nonCreaturePermanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void riseCannotTargetANonCreatureCardInAGraveyard() {
        Card nonCreatureCard = new BreedingPool();
        Permanent battlefieldCreature = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());
        harness.setGraveyard(player2, List.of(nonCreatureCard));
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(
                player1, 0, 0, List.of(nonCreatureCard.getId(), battlefieldCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fallDiscardsTheRevealedNonlandCards() {
        harness.setHand(player2, List.of(new AzoriusFirstWing(), new AzoriusFirstWing()));
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void fallKeepsARevealedLandInHand() {
        Card land = new BreedingPool();
        Card creature = new AzoriusFirstWing();
        harness.setHand(player2, List.of(land, creature));
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void fallDiscardsTheOnlyRevealedNonlandCardWhenTargetHasFewerThanTwoCards() {
        Card creature = new AzoriusFirstWing();
        harness.setHand(player2, List.of(creature));
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void riseStillReturnsTheBattlefieldCreatureWhenTheGraveyardTargetLeaves() {
        Card graveyardCreature = new AzoriusFirstWing();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        Permanent battlefieldCreature = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalSorcery(player1, 0, 0,
                List.of(graveyardCreature.getId(), battlefieldCreature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(graveyardCreature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(battlefieldCreature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(graveyardCreature);
        assertThat(gd.findExiledCard(graveyardCreature.getId())).isNotNull();
    }

    @Test
    void riseStillReturnsTheGraveyardCreatureWhenTheBattlefieldTargetLeaves() {
        Card graveyardCreature = new AzoriusFirstWing();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        Permanent battlefieldCreature = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalSorcery(player1, 0, 0,
                List.of(graveyardCreature.getId(), battlefieldCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(battlefieldCreature);
        harness.setGraveyard(player2, List.of(battlefieldCreature.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(graveyardCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(battlefieldCreature.getCard());
    }

    @Test
    void riseRequiresBothTargetsWhenCast() {
        Permanent battlefieldCreature = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(
                player1, 0, 0, List.of(battlefieldCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void riseDoesNotReturnEitherCardWhenBothTargetsLeaveTheirZones() {
        Card graveyardCreature = new AzoriusFirstWing();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        Permanent battlefieldCreature = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());
        RiseFall spell = new RiseFall();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalSorcery(player1, 0, 0,
                List.of(graveyardCreature.getId(), battlefieldCreature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(graveyardCreature));
        gd.playerBattlefields.get(player2.getId()).remove(battlefieldCreature);
        harness.setGraveyard(player2, List.of(battlefieldCreature.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(battlefieldCreature.getCard());
        assertThat(gd.findExiledCard(graveyardCreature.getId())).isNotNull();
    }

    @Test
    void riseCannotBePaidForWithFallsColors() {
        Card graveyardCreature = new AzoriusFirstWing();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        Permanent battlefieldCreature = harness.addToBattlefieldAndReturn(player2, new AzoriusFirstWing());
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0,
                List.of(graveyardCreature.getId(), battlefieldCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fallCannotBePaidForWithRisesColors() {
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fallDoesNothingToAnEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void fallKeepsBothRevealedLands() {
        Card firstLand = new BreedingPool();
        Card secondLand = new BreedingPool();
        harness.setHand(player2, List.of(firstLand, secondLand));
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstLand, secondLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void fallCanTargetItsController() {
        Card creature = new AzoriusFirstWing();
        Card land = new BreedingPool();
        RiseFall spell = new RiseFall();
        harness.setHand(player1, List.of(spell, creature, land));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalSorcery(player1, 0, 1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(creature, spell);
    }

    @Test
    void fallDiscardsExactlyTwoDistinctCardsFromALargerNonlandHand() {
        List<Card> cards = List.of(new AzoriusFirstWing(), new AzoriusFirstWing(), new AzoriusFirstWing());
        harness.setHand(player2, cards);
        harness.setHand(player1, List.of(new RiseFall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalSorcery(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2).doesNotHaveDuplicates();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .doesNotContainAnyElementsOf(gd.playerHands.get(player2.getId()));
        assertThat(cards).containsAll(gd.playerGraveyards.get(player2.getId()));
    }
}
