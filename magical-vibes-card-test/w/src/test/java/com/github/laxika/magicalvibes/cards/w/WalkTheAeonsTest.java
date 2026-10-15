package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkTheAeons.class, Island.class, Forest.class})
class WalkTheAeonsTest extends BaseCardTest {

    @Test
    @DisplayName("Grants an extra turn to the targeted player")
    void grantsExtraTurnToTargetedPlayer() {
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.extraTurns).containsExactly(player2.getId());
        harness.assertInGraveyard(player1, "Walk the Aeons");
    }

    @Test
    @DisplayName("Buyback sacrifices three Islands and returns Walk the Aeons to hand")
    void buybackSacrificesThreeIslandsAndReturnsToHand() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent thirdIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        prepareCast();

        harness.castSorceryWithSacrificesAndBuyback(player1, 0, player2.getId(),
                List.of(firstIsland.getId(), secondIsland.getId(), thirdIsland.getId()));
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player2.getId());
        harness.assertInHand(player1, "Walk the Aeons");
        harness.assertNotInGraveyard(player1, "Walk the Aeons");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Buyback requires three distinct Islands")
    void buybackRequiresThreeDistinctIslands() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorceryWithSacrificesAndBuyback(
                player1, 0, player2.getId(), List.of(island.getId(), island.getId(), island.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Walk the Aeons");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Buyback cannot sacrifice a non-Island land")
    void buybackRequiresIslands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorceryWithSacrificesAndBuyback(
                player1, 0, player2.getId(),
                List.of(forest.getId(), firstIsland.getId(), secondIsland.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only players can be targeted")
    void onlyPlayersCanBeTargeted() {
        harness.addToBattlefield(player2, new Island());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Island")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Buyback is optional even when three Islands are available")
    void canTargetSelfWithoutPayingBuyback() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Walk the Aeons");
        harness.assertNotInHand(player1, "Walk the Aeons");
    }

    @Test
    @DisplayName("Tapped Islands can pay buyback and are sacrificed before resolution")
    void tappedIslandsCanPayBuyback() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Island());
        first.tap();
        second.tap();
        third.tap();
        prepareCast();

        harness.castSorceryWithSacrificesAndBuyback(player1, 0, player1.getId(),
                List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertNotInHand(player1, "Walk the Aeons");
        assertThat(gd.extraTurns).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        harness.assertInHand(player1, "Walk the Aeons");
        harness.assertNotInGraveyard(player1, "Walk the Aeons");
    }

    @Test
    @DisplayName("Buyback cannot sacrifice an opponent's Island")
    void cannotSacrificeOpponentsIsland() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new Island());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorceryWithSacrificesAndBuyback(
                player1, 0, player1.getId(), List.of(first.getId(), second.getId(), opposing.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Walk the Aeons");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.stack).isEmpty();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new WalkTheAeons()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
