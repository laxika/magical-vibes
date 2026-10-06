package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CascadeSeer;
import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.cards.k.KorCelebrant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeafloorStalker.class, CascadeSeer.class, CliffhavenSellSword.class, KorCelebrant.class})
class SeafloorStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("A full party reduces the ability's cost by four generic mana")
    void fullPartyReducesActivationCost() {
        Permanent stalker = addCreatureReady(player1, new SeafloorStalker());
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.addToBattlefield(player1, new CliffhavenSellSword());
        harness.addToBattlefield(player1, new CascadeSeer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stalker), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability boosts Seafloor Stalker and makes it unblockable until cleanup")
    void boostsAndMakesUnblockableUntilCleanup() {
        Permanent stalker = addCreatureReady(player1, new SeafloorStalker());
        Permanent blocker = addCreatureReady(player2, new CliffhavenSellSword());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stalker), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, stalker)).isTrue();

        stalker.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> declareBlock(blocker, stalker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");

        gs.declareBlockers(gd, player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, stalker)).isFalse();
    }

    @Test
    @DisplayName("Duplicate Rogues and an opponent's party do not reduce the activation cost")
    void duplicateRolesAndOpposingCreaturesDoNotReduceCost() {
        Permanent stalker = addCreatureReady(player1, new SeafloorStalker());
        harness.addToBattlefield(player1, new SeafloorStalker());
        harness.addToBattlefield(player2, new KorCelebrant());
        harness.addToBattlefield(player2, new CliffhavenSellSword());
        harness.addToBattlefield(player2, new CascadeSeer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(stalker);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, index, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Repeated activations stack and do not require tapping or haste")
    void repeatedActivationsStackOnSummoningSickCreature() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new SeafloorStalker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, stalker)).isTrue();
        assertThat(stalker.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Even a full party cannot remove the blue mana requirement")
    void fullPartyStillRequiresBlueMana() {
        addCreatureReady(player1, new SeafloorStalker());
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.addToBattlefield(player1, new CliffhavenSellSword());
        harness.addToBattlefield(player1, new CascadeSeer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
