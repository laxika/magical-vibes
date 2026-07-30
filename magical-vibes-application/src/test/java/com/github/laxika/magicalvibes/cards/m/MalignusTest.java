package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MalignusTest extends BaseCardTest {

    @Test
    @DisplayName("P/T is half the opponent's life total (20 -> 10)")
    void ptIsHalfOpponentLife() {
        Permanent malignus = addMalignus(player1);

        assertThat(gqs.getEffectivePower(gd, malignus)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, malignus)).isEqualTo(10);
    }

    @Test
    @DisplayName("An odd opponent life total is rounded up (19 -> 10)")
    void roundsUp() {
        Permanent malignus = addMalignus(player1);
        gd.playerLifeTotals.put(player2.getId(), 19);

        assertThat(gqs.getEffectivePower(gd, malignus)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, malignus)).isEqualTo(10);
    }

    @Test
    @DisplayName("The controller's own life total is ignored")
    void ignoresControllerLife() {
        Permanent malignus = addMalignus(player1);
        gd.playerLifeTotals.put(player1.getId(), 40);
        gd.playerLifeTotals.put(player2.getId(), 8);

        assertThat(gqs.getEffectivePower(gd, malignus)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, malignus)).isEqualTo(4);
    }

    @Test
    @DisplayName("P/T tracks the opponent's life total as it changes")
    void ptUpdatesWithOpponentLife() {
        Permanent malignus = addMalignus(player1);
        gd.playerLifeTotals.put(player2.getId(), 7);

        assertThat(gqs.getEffectivePower(gd, malignus)).isEqualTo(4);

        gd.playerLifeTotals.put(player2.getId(), 1);

        assertThat(gqs.getEffectivePower(gd, malignus)).isEqualTo(1);
    }

    @Test
    @DisplayName("A prevention shield does not stop Malignus's combat damage to a player")
    void combatDamageToPlayerCantBePrevented() {
        addMalignus(player1);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("The same shield does prevent an ordinary creature's combat damage")
    void ordinaryCombatDamageIsPrevented() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A blocker's prevention shield does not stop Malignus's combat damage")
    void combatDamageToBlockerCantBePrevented() {
        addMalignus(player1);
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setDamagePreventionShield(10);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Blanket prevention of all damage to a player does not stop Malignus")
    void blanketPlayerPreventionCantStopMalignus() {
        addMalignus(player1);
        gd.playersWithAllDamagePrevented.add(player2.getId());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Blanket prevention of all damage to creatures does not stop Malignus")
    void blanketCreaturePreventionCantStopMalignus() {
        addMalignus(player1);
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        gd.preventAllDamageToAllCreatures = true;

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    private Permanent addMalignus(Player player) {
        Card card = new Malignus();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
