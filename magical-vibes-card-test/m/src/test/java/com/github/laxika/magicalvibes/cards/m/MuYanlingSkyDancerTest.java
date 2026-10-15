package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({MuYanlingSkyDancer.class, AirElemental.class, Island.class, Shock.class})
class MuYanlingSkyDancerTest extends BaseCardTest {

    @Test
    @DisplayName("+2 reduces power and removes flying until Mu Yanling's next turn")
    void plusTwoDebuffsUntilNextTurn() {
        addReadyMuYanling(player1, 3);
        Permanent elemental = addReadyCreature(player2, new AirElemental());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(elemental.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

        endTurn(player1);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

        endTurn(player2);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("+2 can resolve without choosing a target")
    void plusTwoAllowsNoTarget() {
        Permanent muYanling = addReadyMuYanling(player1, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(muYanling.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-3 creates a 4/4 blue Elemental Bird with flying")
    void minusThreeCreatesElementalBird() {
        addReadyMuYanling(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Elemental Bird"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("-8 gives Islands you control a tap-to-draw ability")
    void minusEightLetsIslandsDraw() {
        addReadyMuYanling(player1, 8);
        Permanent island = addIsland(player1);
        addIsland(player2);
        Card shock = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(shock));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        int islandIndex = gd.playerBattlefields.get(player1.getId()).indexOf(island);
        harness.activateAbility(player1, islandIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(island.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+2 still resolves after Mu Yanling leaves the battlefield")
    void plusTwoResolvesAfterSourceDies() {
        Permanent muYanling = addReadyMuYanling(player1, 1);
        Permanent elemental = addReadyCreature(player2, new AirElemental());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(elemental.getId()));
        harness.castInstant(player2, 0, muYanling.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, muYanling.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(muYanling);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The emblem grants its ability to Islands entering after Mu Yanling dies")
    void emblemAppliesToLaterIslands() {
        Permanent muYanling = addReadyMuYanling(player1, 8);
        Card shock = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(shock));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(muYanling);

        Permanent island = harness.enterBattlefieldAndReturn(player1, new Island());
        int islandIndex = gd.playerBattlefields.get(player1.getId()).indexOf(island);
        harness.activateAbility(player1, islandIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(island.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
    }

    private Permanent addReadyMuYanling(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MuYanlingSkyDancer());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addIsland(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Island());
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Player nextPlayer = activePlayer == player1 ? player2 : player1;
        harness.passUntil(nextPlayer, TurnStep.UPKEEP);
    }
}
