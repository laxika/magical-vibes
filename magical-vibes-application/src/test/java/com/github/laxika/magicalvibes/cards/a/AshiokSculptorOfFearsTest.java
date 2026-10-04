package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({AshiokSculptorOfFears.class, Forest.class, GrizzlyBears.class})
class AshiokSculptorOfFearsTest extends BaseCardTest {

    @Test
    @DisplayName("+2 draws a card and makes each player mill two cards")
    void plusTwoDrawsAndMills() {
        Permanent ashiok = addReadyAshiok(player1, 4);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-5 returns a target creature card from any graveyard under its controller's control")
    void minusFiveReanimatesCreatureFromAnyGraveyard() {
        Permanent ashiok = addReadyAshiok(player1, 5);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-5 cannot target a noncreature card")
    void minusFiveCannotTargetNoncreatureCard() {
        addReadyAshiok(player1, 5);
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-11 gains control of all creatures controlled by the targeted opponent")
    void minusElevenGainsControlOfOpponentsCreatures() {
        Permanent ashiok = addReadyAshiok(player1, 11);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature, opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentLand);
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    void minusElevenCannotTargetItsController() {
        addReadyAshiok(player1, 11);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAshiok(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AshiokSculptorOfFears());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
