package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HauntedGuardian.class, MoorlandInquisitor.class})
class HauntedGuardianTest extends BaseCardTest {

    @Test
    void defenderPreventsAttackingEvenAfterSummoningSicknessEnds() {
        addCreatureReady(player1, new HauntedGuardian());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstStrikeKillsBlockedAttackerBeforeItCanDealDamage() {
        Permanent attacker = addCreatureReady(player1, new MoorlandInquisitor());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new HauntedGuardian(), "{2}");
        harness.passBothPriorities();
        Permanent guardian = findPermanent(player2, "Haunted Guardian");

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Moorland Inquisitor");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(guardian);
        assertThat(guardian.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void firstStrikeAttackerAndGuardianDealDamageSimultaneously() {
        Permanent attacker = addCreatureReady(player1, new MoorlandInquisitor());
        Permanent guardian = addCreatureReady(player2, new HauntedGuardian());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(guardian);
        harness.assertInGraveyard(player1, "Moorland Inquisitor");
        harness.assertInGraveyard(player2, "Haunted Guardian");
        harness.assertLife(player2, 20);
    }
}
