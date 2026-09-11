package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenalishMissionary.class, BenalishKnight.class})
class BenalishMissionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the blocked attacker's combat damage, so the blocker survives")
    void preventsBlockedAttackerCombatDamage() {
        Permanent missionary = addCreatureReady(player2, new BenalishMissionary());
        Permanent attacker = addCreatureReady(player1, new BenalishKnight());
        addCreatureReady(player2, new BenalishKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, battlefieldIndex(player2, missionary), null, attacker.getId());
        assertThat(missionary.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        resolveCombat(player1);

        assertThat(countPermanents(player2, "Benalish Knight")).isEqualTo(1);
        assertThat(countPermanents(player1, "Benalish Knight")).isZero();
    }

    @Test
    @DisplayName("Cannot target an unblocked attacker")
    void cannotTargetUnblockedAttacker() {
        Permanent missionary = addCreatureReady(player2, new BenalishMissionary());
        Permanent attacker = addCreatureReady(player1, new BenalishKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.addMana(player2, ManaColor.WHITE, 2);

        int index = battlefieldIndex(player2, missionary);
        UUID targetId = attacker.getId();
        assertThatThrownBy(() -> harness.activateAbility(player2, index, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the blocker instead of the blocked attacker")
    void cannotTargetTheBlocker() {
        Permanent missionary = addCreatureReady(player2, new BenalishMissionary());
        addCreatureReady(player1, new BenalishKnight());
        Permanent blocker = addCreatureReady(player2, new BenalishKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        harness.addMana(player2, ManaColor.WHITE, 2);

        int index = battlefieldIndex(player2, missionary);
        assertThatThrownBy(() -> harness.activateAbility(player2, index, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still targets the blocked attacker if the blocker leaves before resolution")
    void stillTargetsBlockedAttackerAfterBlockerLeaves() {
        Permanent missionary = addCreatureReady(player2, new BenalishMissionary());
        Permanent attacker = addCreatureReady(player1, new BenalishKnight());
        Permanent blocker = addCreatureReady(player2, new BenalishKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, battlefieldIndex(player2, missionary), null, attacker.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, blocker));
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    private int battlefieldIndex(Player owner, Permanent permanent) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(permanent);
    }
}
