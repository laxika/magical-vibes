package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BadRiver;
import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShaukuEndbringer.class, BayFalcon.class, BadRiver.class})
class ShaukuEndbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Shauku can attack when it is the only creature on the battlefield")
    void canAttackAlone() {
        harness.setLife(player2, 20);
        Permanent shauku = addCreatureReady(player1, new ShaukuEndbringer());

        declareAttackers(player1, List.of(findIndex(player1, shauku)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Shauku cannot attack while another creature is on the battlefield")
    void cannotAttackWithAnotherCreature() {
        Permanent shauku = addCreatureReady(player1, new ShaukuEndbringer());
        harness.addToBattlefield(player2, new BayFalcon());

        int index = findIndex(player1, shauku);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shauku can attack while another noncreature permanent is on the battlefield")
    void canAttackWithAnotherNoncreaturePermanent() {
        harness.setLife(player2, 20);
        Permanent shauku = addCreatureReady(player1, new ShaukuEndbringer());
        harness.addToBattlefield(player2, new BadRiver());

        declareAttackers(player1, List.of(findIndex(player1, shauku)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Controller loses 3 life at the beginning of their upkeep")
    void upkeepLifeLoss() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ShaukuEndbringer());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Tap ability exiles the target creature and puts a +1/+1 counter on Shauku")
    void exilesTargetAndGrowsSelf() {
        Permanent shauku = addCreatureReady(player1, new ShaukuEndbringer());
        harness.addToBattlefield(player2, new BayFalcon());

        UUID targetId = harness.getPermanentId(player2, "Bay Falcon");
        harness.activateAbility(player1, findIndex(player1, shauku), null, targetId);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Bay Falcon")).isZero();
        assertThat(shauku.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, shauku)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, shauku)).isEqualTo(6);
    }

    @Test
    @DisplayName("Tap ability cannot target a noncreature permanent")
    void tapAbilityRequiresCreatureTarget() {
        Permanent shauku = addCreatureReady(player1, new ShaukuEndbringer());
        Permanent badRiver = harness.addToBattlefieldAndReturn(player2, new BadRiver());

        assertThatThrownBy(() -> harness.activateAbility(player1, findIndex(player1, shauku), null,
                badRiver.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(shauku.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tap ability does not put a counter on Shauku if its target leaves before resolution")
    void targetLeavingBeforeResolutionFizzlesTheAbility() {
        Permanent shauku = addCreatureReady(player1, new ShaukuEndbringer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BayFalcon());

        harness.activateAbility(player1, findIndex(player1, shauku), null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shauku)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, shauku)).isEqualTo(5);
    }

    private int findIndex(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        List<Permanent> bf = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i) == target) return i;
        }
        throw new IllegalStateException("Permanent not found on battlefield");
    }
}
