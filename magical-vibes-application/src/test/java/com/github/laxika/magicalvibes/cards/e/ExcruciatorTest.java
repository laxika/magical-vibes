package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulsFire;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
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

@CardUsed({Excruciator.class, GrizzlyBears.class, SoulsFire.class, TurnToFrog.class})
class ExcruciatorTest extends BaseCardTest {

    @Test
    @DisplayName("Its combat damage to a player can't be prevented")
    void combatDamageToPlayerCantBePrevented() {
        addExcruciatorReady(player1);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Prevent all combat damage still prevents damage from other sources")
    void preventAllCombatDamageStillPreventsOtherSources() {
        addExcruciatorReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        gd.preventAllCombatDamage = true;

        declareAttackers(List.of(0, 1));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Its combat damage to a blocking creature can't be prevented")
    void combatDamageToBlockerCantBePrevented() {
        addExcruciatorReady(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setDamagePreventionShield(10);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Its noncombat damage to a player can't be prevented")
    void noncombatDamageToPlayerCantBePrevented() {
        Permanent excruciator = addExcruciatorReady(player1);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(excruciator.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Its noncombat damage to a creature can't be prevented")
    void noncombatDamageToCreatureCantBePrevented() {
        Permanent source = addExcruciatorReady(player1);
        Permanent victim = addExcruciatorReady(player2);
        victim.setDamagePreventionShield(10);
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(victim.getId()));
    }

    @Test
    @DisplayName("Losing its abilities allows its combat damage to be prevented")
    void combatDamageCanBePreventedAfterLosingAbilities() {
        Permanent source = addExcruciatorReady(player1);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Losing its abilities allows its noncombat damage to be prevented")
    void noncombatDamageCanBePreventedAfterLosingAbilities() {
        Permanent source = addExcruciatorReady(player1);
        harness.setHand(player1, List.of(new TurnToFrog(), new SoulsFire()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        harness.castInstant(player1, 0, List.of(source.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(9);
    }

    private Permanent addExcruciatorReady(Player player) {
        return addCreatureReady(player, new Excruciator());
    }
}
