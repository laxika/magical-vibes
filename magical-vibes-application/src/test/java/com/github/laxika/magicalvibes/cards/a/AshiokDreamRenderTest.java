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

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    private Permanent addReadyAshiok(Player player, int loyalty) {
        Permanent ashiok = new Permanent(new AshiokDreamRender());
        ashiok.setCounterCount(CounterType.LOYALTY, loyalty);
        ashiok.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ashiok);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ashiok;
    }
}
