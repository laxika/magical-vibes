package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Divination.class, InvasionOfArcavios.class, InvocationOfTheFounders.class,
        LightningBolt.class, Shock.class})
class InvasionOfArcaviosTest extends BaseCardTest {

    @Test
    @DisplayName("The Siege searches the library, graveyard, and sideboard for an instant or sorcery")
    void searchesAllAllowedZones() {
        Card libraryCard = new Shock();
        Card graveyardCard = new Divination();
        Card sideboardCard = new LightningBolt();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(libraryCard);
        harness.setGraveyard(player1, List.of(graveyardCard));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(sideboardCard)));

        castInvasion();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.pool()).containsExactlyInAnyOrder(libraryCard, graveyardCard, sideboardCard);
        assertThat(choice.libraryCardIds()).containsExactly(libraryCard.getId());
        assertThat(choice.outsideGameCardIds()).containsExactly(sideboardCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(sideboardCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(sideboardCard);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
    }

    @Test
    @DisplayName("Defeating the Siege exiles it and casts Invocation of the Founders transformed")
    void defeatCastsBackFace() {
        gd.playerDecks.get(player1.getId()).clear();
        castInvasion();

        Permanent battle = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Invasion of Arcavios".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent backFace = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Invocation of the Founders".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(backFace.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Invocation of the Founders may copy an instant cast from hand")
    void backFaceCopiesInstantFromHand() {
        InvasionOfArcavios front = new InvasionOfArcavios();
        Permanent invocation = harness.addToBattlefieldAndReturn(player1, front);
        invocation.setCard(front.getBackFaceCard());
        invocation.setTransformed(true);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).extracting(entry -> entry.getCard().getName())
                .contains("Shock");
    }

    private void castInvasion() {
        harness.setHand(player1, List.of(new InvasionOfArcavios()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
