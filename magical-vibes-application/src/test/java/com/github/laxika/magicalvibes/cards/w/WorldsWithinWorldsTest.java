package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorldsWithinWorlds.class, GrizzlyBears.class, Forest.class, MishrasFactory.class})
class WorldsWithinWorldsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles creatures, collects hidden choices, returns the exiled cards, and exiles itself")
    void resolvesAllStepsInOrder() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        Card ownHandCreature = new GrizzlyBears();
        Card opponentHandCreature = new GrizzlyBears();
        Card ownLand = new Forest();

        harness.setHand(player1, List.of(new WorldsWithinWorlds(), ownHandCreature, ownLand));
        harness.setHand(player2, List.of(opponentHandCreature));
        harness.addToBattlefield(player1, ownCreature);
        harness.addToBattlefield(player2, opponentCreature);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.WorldsWithinWorldsChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(ownHandCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.WorldsWithinWorldsChoice.class);

        harness.handleMultipleCardsChosen(player2, List.of(opponentHandCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(ownHandCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(opponentHandCreature.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(ownCreature.getId(), ownLand.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(opponentCreature.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Worlds Within Worlds");
        harness.assertNotInGraveyard(player1, "Worlds Within Worlds");
    }

    @Test
    void resolvesWithNoCreaturesOrChoices() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new WorldsWithinWorlds(), "{5}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).containsExactly("Worlds Within Worlds");
        harness.assertNotInGraveyard(player1, "Worlds Within Worlds");
    }

    @Test
    void playersMayDeclineOrChooseOnlySomeCreatures() {
        Card declined = new GrizzlyBears();
        Card selected = new GrizzlyBears();
        Card retained = new GrizzlyBears();
        harness.setHand(player2, List.of(selected, retained));
        harness.castFromHand(player1, new WorldsWithinWorlds(), "{5}{G}{U}");
        harness.setHand(player1, List.of(declined));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player2, List.of(selected.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(selected.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(declined);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
    }

    @Test
    void returnsExiledCreaturesWhenNeitherPlayerHasCreatureCardsInHand() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.addToBattlefield(player2, creature);
        harness.addToBattlefield(player2, land);
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new WorldsWithinWorlds(), "{5}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(land.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void returnsAnAnimatedLandToItsOwnersHand() {
        Card factory = new MishrasFactory();
        harness.addToBattlefield(player1, factory);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new WorldsWithinWorlds(), "{5}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(factory);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).doesNotContain(factory.getId());
    }
}
