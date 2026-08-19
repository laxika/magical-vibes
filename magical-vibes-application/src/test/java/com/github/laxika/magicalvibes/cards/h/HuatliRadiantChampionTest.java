package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HuatliRadiantChampionTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts one loyalty counter on Huatli for each creature you control")
    void plusOneCountsOnlyControlledCreatures() {
        Permanent huatli = addReadyHuatli(player1, 3);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-1 gives target creature +X/+X based on creatures you control")
    void minusOneScalesWithControlledCreatures() {
        Permanent huatli = addReadyHuatli(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("-8 emblem may draw when a controlled creature enters")
    void minusEightEmblemDrawsForControlledCreature() {
        addReadyHuatli(player1, 8);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("-8 emblem does not trigger for an opponent's creature")
    void minusEightEmblemIgnoresOpponentCreature() {
        addReadyHuatli(player1, 8);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyHuatli(Player player, int loyalty) {
        Permanent perm = new Permanent(new HuatliRadiantChampion());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
