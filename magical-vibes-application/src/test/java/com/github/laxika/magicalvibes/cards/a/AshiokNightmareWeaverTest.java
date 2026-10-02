package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SparkJolt;
import com.github.laxika.magicalvibes.cards.v.VoyagingSatyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshiokNightmareWeaver.class, Forest.class, VoyagingSatyr.class, SparkJolt.class})
class AshiokNightmareWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("+2 exiles the top three cards of the targeted opponent's library with Ashiok")
    void plusTwoExilesTopThreeCardsWithAshiok() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        List<Card> topCards = List.of(new Forest(), new VoyagingSatyr(), new SparkJolt());
        harness.setLibrary(player2, topCards);

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
        Card firstBear = new VoyagingSatyr();
        Card chosenBear = new VoyagingSatyr();
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
        Card ownHand = new SparkJolt();
        Card opponentHand = new Forest();
        Card ownGraveyard = new VoyagingSatyr();
        Card opponentGraveyard = new SparkJolt();
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

    @Test
    void plusTwoExilesOnlyAvailableCardsFromShortLibrary() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        Card creature = new VoyagingSatyr();
        harness.setLibrary(player2, List.of(creature));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(ashiok.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void minusXRequiresExactManaValueEvenWhenNoCreatureMatches() {
        Permanent ashiok = addReadyAshiok(player1, 5);
        Card creature = new VoyagingSatyr();
        gd.addToExile(player2.getId(), creature, ashiok.getId());

        harness.activateAbility(player1, 0, 1, 3, null);
        harness.passBothPriorities();

        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ashiok);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusZeroDoesNotReturnNoncreatureCards() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        Card land = new Forest();
        gd.addToExile(player2.getId(), land, ashiok.getId());

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ashiok);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusXCannotReturnCreatureExiledByAnotherAshiok() {
        Permanent oldAshiok = addReadyAshiok(player1, 3);
        Card creature = new VoyagingSatyr();
        gd.addToExile(player2.getId(), creature, oldAshiok.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent newAshiok = addReadyAshiok(player1, 3);

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(newAshiok);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
    }

    @Test
    void minusXReturnsOpponentCreatureAfterSpendingAllLoyalty() {
        Permanent ashiok = addReadyAshiok(player1, 2);
        Card creature = new VoyagingSatyr();
        gd.addToExile(player2.getId(), creature, ashiok.getId());

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ashiok.getCard());
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned))
                .contains(CardSubtype.NIGHTMARE, CardSubtype.SATYR, CardSubtype.DRUID);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(creature);
    }

    @Test
    void minusXCanReturnCreatureExiledFromHandByMinusTen() {
        assertUltimateExiledCreatureCanReturn(true);
    }

    @Test
    void minusXCanReturnCreatureExiledFromGraveyardByMinusTen() {
        assertUltimateExiledCreatureCanReturn(false);
    }

    private void assertUltimateExiledCreatureCanReturn(boolean fromHand) {
        addReadyAshiok(player1, 13);
        Card creature = new VoyagingSatyr();
        harness.setHand(player1, List.of());
        harness.setHand(player2, fromHand ? List.of(creature) : List.of());
        harness.setGraveyard(player2, fromHand ? List.of() : List.of(creature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.NIGHTMARE);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(creature);
    }

    private Permanent addReadyAshiok(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AshiokNightmareWeaver());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
