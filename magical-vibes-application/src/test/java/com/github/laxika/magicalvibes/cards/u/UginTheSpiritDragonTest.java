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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    private List<Card> forests(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }

    private Permanent addReadyUgin(Player player, int loyalty) {
        UginTheSpiritDragon card = new UginTheSpiritDragon();
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
