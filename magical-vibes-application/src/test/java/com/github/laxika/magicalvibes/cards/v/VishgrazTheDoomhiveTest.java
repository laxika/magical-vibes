package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VishgrazTheDoomhive.class)
class VishgrazTheDoomhiveTest extends BaseCardTest {

    private void castAndResolve() {
        harness.setHand(player1, List.of(new VishgrazTheDoomhive()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering the battlefield creates three Mites that can't block")
    void enteringBattlefieldCreatesThreeMites() {
        castAndResolve();

        List<Permanent> mites = findPermanents(player1, "Mite");
        assertThat(mites).hasSize(3);
        assertThat(mites).allSatisfy(mite -> assertThat(bls.canBlock(gd, mite)).isFalse());
    }

    @Test
    @DisplayName("A Mite dealing combat damage gives a poison counter")
    void miteCombatDamageGivesPoisonCounter() {
        castAndResolve();

        Permanent mite = findPermanent(player1, "Mite");
        mite.setSummoningSick(false);
        mite.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 for each poison counter opponents have")
    void boostsForOpponentsPoisonCounters() {
        harness.addToBattlefield(player1, new VishgrazTheDoomhive());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        Permanent vishgraz = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, vishgraz)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, vishgraz)).isEqualTo(6);
    }
}
