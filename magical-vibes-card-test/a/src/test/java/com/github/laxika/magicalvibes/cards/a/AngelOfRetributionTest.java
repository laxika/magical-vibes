package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LaquatussChampion;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelOfRetribution.class, Aquamoeba.class, LaquatussChampion.class})
class AngelOfRetributionTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Angel of Retribution")
    void flyingPreventsGroundBlocker() {
        Permanent blocker = addCreatureReady(player2, new Aquamoeba());
        Permanent angel = addCreatureReady(player1, new AngelOfRetribution());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(angel);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Flying does not prevent Angel of Retribution from blocking a ground creature")
    void flyingDoesNotPreventBlockingGroundCreature() {
        Permanent attacker = addCreatureReady(player1, new Aquamoeba());
        Permanent angel = addCreatureReady(player2, new AngelOfRetribution());

        declareAttackersAndPrepareBlockers(List.of(0));

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(angel);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(angel.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("First strike defeats Laquatus's Champion before it deals regular combat damage")
    void firstStrikeDealsDamageBeforeRegularCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new LaquatussChampion());
        Permanent angel = addCreatureReady(player2, new AngelOfRetribution());

        declareAttackersAndPrepareBlockers(List.of(0));

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(angel);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(angel);
    }

    @Test
    @DisplayName("Flying creatures can block the Angel and first strike damage is simultaneous")
    void flyingBlockerWithFirstStrikeTradesWithAngel() {
        Permanent attacker = addCreatureReady(player1, new AngelOfRetribution());
        Permanent blocker = addCreatureReady(player2, new AngelOfRetribution());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player1, "Angel of Retribution");
        harness.assertInGraveyard(player2, "Angel of Retribution");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked Angel deals combat damage once despite first strike")
    void unblockedAngelDealsDamageOnlyOnce() {
        addCreatureReady(player1, new AngelOfRetribution());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 15);
    }
}
