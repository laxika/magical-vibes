package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AshiokNightmareWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("+2 exiles the top three cards of the targeted opponent's library with Ashiok")
    void plusTwoExilesTopThreeCardsWithAshiok() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        List<Card> topCards = List.of(new Forest(), new GrizzlyBears(), new Shock());
        harness.setLibrary(player2, new ArrayList<>(topCards));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.getCardsExiledByPermanent(ashiok.getId())).containsExactlyElementsOf(topCards);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("+2 cannot target its controller")
    void plusTwoCannotTargetSelf() {
        addReadyAshiok(player1, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-X returns a matching creature exiled with Ashiok and grants it Nightmare")
    void minusXReturnsMatchingCreatureAsNightmare() {
        Permanent ashiok = addReadyAshiok(player1, 5);
        Card firstBear = new GrizzlyBears();
        Card chosenBear = new GrizzlyBears();
        Card land = new Forest();
        gd.addToExile(player1.getId(), firstBear, ashiok.getId());
        gd.addToExile(player1.getId(), chosenBear, ashiok.getId());
        gd.addToExile(player1.getId(), land, ashiok.getId());

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosenBear.getId()));

        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(chosenBear.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.NIGHTMARE);
        assertThat(gd.getCardsExiledByPermanent(ashiok.getId()))
                .containsExactlyInAnyOrder(firstBear, land);
    }

    @Test
    @DisplayName("-10 exiles all cards from opponents' hands and graveyards")
    void minusTenExilesOpponentsHandsAndGraveyards() {
        Permanent ashiok = addReadyAshiok(player1, 11);
        Card ownHand = new Shock();
        Card opponentHand = new Forest();
        Card ownGraveyard = new GrizzlyBears();
        Card opponentGraveyard = new Shock();
        harness.setHand(player1, List.of(ownHand));
        harness.setHand(player2, List.of(opponentHand));
        harness.setGraveyard(player1, List.of(ownGraveyard));
        harness.setGraveyard(player2, List.of(opponentGraveyard));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownHand);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownGraveyard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(opponentHand, opponentGraveyard);
    }

    private Permanent addReadyAshiok(Player player, int loyalty) {
        Permanent perm = new Permanent(new AshiokNightmareWeaver());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
