package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepeatedReverberationTest extends BaseCardTest {

    @Test
    @DisplayName("copies the next instant or sorcery spell twice")
    void copiesNextSpellTwice() {
        castRepeatedReverberation();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("copies the next loyalty ability twice")
    void copiesNextLoyaltyAbilityTwice() {
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent elemental = harness.addToBattlefieldAndReturn(player1,
                new com.github.laxika.magicalvibes.cards.a.AirElemental());
        castRepeatedReverberation();

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextLoyaltyAbilityCopyThisTurnCount.get(player1.getId())).isEqualTo(2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.pendingNextLoyaltyAbilityCopyThisTurnCount).doesNotContainKey(player1.getId());

        resolveAllTriggers();

        assertThat(elemental.getPowerModifier()).isEqualTo(6);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    private void castRepeatedReverberation() {
        harness.setHand(player1, List.of(new RepeatedReverberation()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(2);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraNovicePyromancer());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
