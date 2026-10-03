package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StormSpirit;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrogskolReinforcements.class, StormSpirit.class, GrizzlyBears.class, Shock.class})
class DrogskolReinforcementsTest extends BaseCardTest {

    @Test
    @DisplayName("Other Spirits you control have melee")
    void grantsMeleeToOtherSpiritsYouControl() {
        harness.addToBattlefield(player1, new DrogskolReinforcements());
        Permanent spirit = addCreatureReady(player1, new StormSpirit());
        Permanent nonSpirit = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentSpirit = addCreatureReady(player2, new StormSpirit());

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.MELEE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonSpirit, Keyword.MELEE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentSpirit, Keyword.MELEE)).isFalse();
    }

    @Test
    @DisplayName("Melee boosts an attacking Spirit")
    void meleeBoostsAttackingSpirit() {
        harness.addToBattlefield(player1, new DrogskolReinforcements());
        Permanent spirit = addCreatureReady(player1, new StormSpirit());
        int powerBefore = gqs.getEffectivePower(gd, spirit);
        int toughnessBefore = gqs.getEffectiveToughness(gd, spirit);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Noncombat damage to Spirits you control is prevented, but not to other creatures")
    void preventsNoncombatDamageOnlyToOwnSpirits() {
        Permanent reinforcements = harness.addToBattlefieldAndReturn(player1, new DrogskolReinforcements());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, reinforcements.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(reinforcements.getMarkedDamage()).isZero();
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to Spirits you control is not prevented")
    void doesNotPreventCombatDamage() {
        Permanent blocker = addCreatureReady(player1, new StormSpirit());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new DrogskolReinforcements());

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }
}
