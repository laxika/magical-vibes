package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UginTheSpiritDragon.class, GrizzlyBears.class, Ornithopter.class,
        SerraAngel.class, Shock.class, Forest.class})
class UginTheSpiritDragonTest extends BaseCardTest {

    @Test
    @DisplayName("+2 deals 3 damage to any target")
    void plusTwoDealsDamageToAnyTarget() {
        Permanent ugin = addReadyUgin(player1, 5);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("-X exiles colored permanents with mana value X or less")
    void minusXExilesMatchingColoredPermanents() {
        Permanent ugin = addReadyUgin(player1, 3);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new SerraAngel());

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Ornithopter", "Serra Angel");
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-10 gains life, draws seven, and puts up to seven permanents from hand onto the battlefield")
    void minusTenResolvesAllEffectsAndCapsHandSelection() {
        Permanent ugin = addReadyUgin(player1, 10);
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            hand.add(new GrizzlyBears());
        }
        Shock shock = new Shock();
        hand.add(shock);
        harness.setHand(player1, hand);
        harness.setLibrary(player1, forests(8));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(7);
        assertThat(choice.validCardIds()).doesNotContain(shock.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 7);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(16);

        List<UUID> chosenIds = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card instanceof GrizzlyBears)
                .limit(7)
                .map(Card::getId)
                .toList();
        harness.handleMultipleCardsChosen(player1, chosenIds);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .hasSize(7);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    void plusTwoDealsLethalDamageToCreature() {
        addReadyUgin(player1, 7);
        GrizzlyBears bears = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, bears);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
    }

    @Test
    void plusTwoDealsDamageToPlaneswalker() {
        addReadyUgin(player1, 7);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UginTheSpiritDragon());
        target.setCounterCount(CounterType.LOYALTY, 7);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void minusZeroLeavesColoredCreaturesAndColorlessPermanents() {
        Permanent ugin = addReadyUgin(player1, 7);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void minusXExilesMatchingPermanentsOnBothSidesEvenWhenUginDiesToCost() {
        addReadyUgin(player1, 2);
        GrizzlyBears ownBears = new GrizzlyBears();
        GrizzlyBears opposingBears = new GrizzlyBears();
        harness.addToBattlefield(player1, ownBears);
        harness.addToBattlefield(player2, opposingBears);

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownBears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opposingBears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof UginTheSpiritDragon);
    }

    @Test
    void ultimateCanPutNewlyDrawnLandOntoBattlefield() {
        addReadyUgin(player1, 10);
        harness.setHand(player1, List.of());
        List<Card> library = forests(8);
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Card drawnLand = gd.playerHands.get(player1.getId()).getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drawnLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(drawnLand);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void ultimateAllowsPuttingNoCardsOntoBattlefield() {
        addReadyUgin(player1, 10);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, forests(8));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    private List<Card> forests(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }

    private Permanent addReadyUgin(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new UginTheSpiritDragon());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
