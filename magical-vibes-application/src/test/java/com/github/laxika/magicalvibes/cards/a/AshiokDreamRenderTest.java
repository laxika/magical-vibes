package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({AshiokDreamRender.class, DiabolicTutor.class, Forest.class, GrizzlyBears.class, Shock.class})
class AshiokDreamRenderTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents cannot search their libraries from spells they control")
    void opponentsCannotSearchTheirLibraries() {
        addReadyAshiok(player1, 5);
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("prevented by Ashiok, Dream Render"));
    }

    @Test
    @DisplayName("Ashiok's ability mills the target, then exiles each opponent's graveyard")
    void millsTargetAndExilesOpponentsGraveyards() {
        Permanent ashiok = addReadyAshiok(player1, 5);
        Card ownGraveyardCard = new Forest();
        Card opponentGraveyardCard = new Shock();
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, library);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownGraveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        List<Card> expectedExile = new java.util.ArrayList<>();
        expectedExile.add(opponentGraveyardCard);
        expectedExile.addAll(library);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrderElementsOf(expectedExile);
    }

    @Test
    @DisplayName("Ashiok does not stop its controller from searching")
    void controllerCanSearchTheirLibrary() {
        addReadyAshiok(player1, 5);
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    @DisplayName("Targeting yourself preserves your milled cards and exiles the opponent's graveyard")
    void canMillControllerWithoutExilingTheirGraveyard() {
        addReadyAshiok(player1, 5);
        Card existingCard = new Forest();
        Card opponentCard = new Shock();
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, List.of(existingCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(4));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(existingCard, library.get(0), library.get(1), library.get(2), library.get(3));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    @DisplayName("A short library is milled completely and the graveyard is still exiled")
    void millsShortLibraryAndStillExilesGraveyard() {
        addReadyAshiok(player1, 5);
        Card milledCard = new Forest();
        Card existingCard = new Shock();
        harness.setLibrary(player2, List.of(milledCard));
        harness.setGraveyard(player2, List.of(existingCard));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(existingCard, milledCard);
    }

    @Test
    @DisplayName("The ability resolves after its last loyalty counter puts Ashiok into the graveyard")
    void abilityResolvesAfterAshiokDiesToLoyaltyCost() {
        Permanent ashiok = addReadyAshiok(player1, 1);
        Card opponentCard = new Forest();
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of(opponentCard));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ashiok);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ashiok.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
    }

    private Permanent addReadyAshiok(Player player, int loyalty) {
        Permanent ashiok = harness.addToBattlefieldAndReturn(player, new AshiokDreamRender());
        ashiok.setCounterCount(CounterType.LOYALTY, loyalty);
        ashiok.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ashiok;
    }
}
