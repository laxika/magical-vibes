package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.r.Ragamuffyn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssaultZeppelid.class, Ragamuffyn.class, MistralCharger.class})
class AssaultZeppelidTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents non-flying creatures from blocking")
    void flyingPreventsGroundBlockers() {
        Permanent zeppelid = addCreatureReady(player1, new AssaultZeppelid());
        Permanent blocker = addCreatureReady(player2, new Ragamuffyn());

        declareAttackersAndPrepareBlockers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(zeppelid)));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zeppelid);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AssaultZeppelid());
        Permanent blocker = addCreatureReady(player2, new MistralCharger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 2
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
    @Test
    @DisplayName("Trample requires lethal damage to the blocker before damaging the player")
    void trampleRequiresLethalDamageBeforeOverflow() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AssaultZeppelid());
        Permanent blocker = addCreatureReady(player2, new MistralCharger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample");

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Mistral Charger");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 2
        ));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Mistral Charger");
        harness.assertOnBattlefield(player1, "Assault Zeppelid");
    }
}
