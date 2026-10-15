package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalefulMastery.class, Forest.class, GrizzlyBears.class, NicolBolasPlaneswalker.class})
class BalefulMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Normal casting exiles target creature without making the opponent draw")
    void normalCastExilesCreatureWithoutBonusDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new BalefulMastery()));
        addNormalMana();
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    @DisplayName("Alternate casting exiles the target and makes the opponent draw")
    void alternateCastExilesTargetAndOpponentDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new BalefulMastery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void canTargetPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new BalefulMastery()));
        addNormalMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BalefulMastery()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    @DisplayName("Alternate casting can exile your own creature and draws only on resolution")
    void alternateCastCanExileOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new BalefulMastery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An illegal target prevents the alternate-cost opponent draw")
    void illegalTargetPreventsOpponentDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new BalefulMastery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof BalefulMastery);
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
