package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SpectralBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AetherSnapTest extends BaseCardTest {

    @Test
    @DisplayName("Removes every counter from every permanent")
    void removesAllCountersFromAllPermanents() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ownCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        opponentArtifact.setCounterCount(CounterType.CHARGE, 3);
        opponentArtifact.setCounterCount(CounterType.LORE, 1);
        castAetherSnap();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(opponentArtifact.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(opponentArtifact.getCounterCount(CounterType.LORE)).isZero();
    }

    @Test
    @DisplayName("Exiles tokens on both battlefields and leaves nontoken permanents")
    void exilesAllTokens() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.addToBattlefield(player1, token(new SpectralBears()));
        harness.addToBattlefield(player2, token(new GrizzlyBears()));
        castAetherSnap();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentPermanent);
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(Card::isToken);
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(Card::isToken);
    }

    private void castAetherSnap() {
        harness.setHand(player1, List.of(new AetherSnap()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Card token(Card card) {
        card.setToken(true);
        return card;
    }
}
