package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantsSkewer.class, AirElemental.class, GrizzlyBears.class, ProdigalPyromancer.class,
        SuntailHawk.class})
class GiantsSkewerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent skewer = addSkewerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        skewer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage to a creature creates a Food token")
    void combatDamageToCreatureCreatesFood() {
        Permanent skewer = addSkewerReady(player1);
        Permanent attacker = addCreatureReady(player1, new SuntailHawk());
        skewer.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new AirElemental());

        declareAttackers(player1, List.of(1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Combat damage to a player does not create a Food token")
    void combatDamageToPlayerDoesNotCreateFood() {
        Permanent skewer = addSkewerReady(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        skewer.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Noncombat damage to a creature does not create a Food token")
    void noncombatDamageToCreatureDoesNotCreateFood() {
        Permanent skewer = addSkewerReady(player1);
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        skewer.setAttachedTo(pyromancer.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private Permanent addSkewerReady(Player player) {
        Permanent skewer = new Permanent(new GiantsSkewer());
        skewer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(skewer);
        return skewer;
    }
}
