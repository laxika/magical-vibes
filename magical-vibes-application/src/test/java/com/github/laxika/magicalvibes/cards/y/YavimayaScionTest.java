package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.r.RingOfGix;
import com.github.laxika.magicalvibes.cards.t.TickingGnomes;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YavimayaScion.class, TickingGnomes.class, GiantCockroach.class, RingOfGix.class})
class YavimayaScionTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from artifacts prevents blocking by an artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        Permanent scion = addCreatureReady(player1, new YavimayaScion());
        scion.setAttacking(true);
        Permanent artifactCreature = addCreatureReady(player2, new TickingGnomes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, artifactCreature), indexOf(player1, scion)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by a non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        Permanent scion = addCreatureReady(player1, new YavimayaScion());
        scion.setAttacking(true);
        Permanent creature = addCreatureReady(player2, new GiantCockroach());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, creature), indexOf(player1, scion))));

        assertThat(creature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from artifacts prevents an artifact ability from targeting Yavimaya Scion")
    void protectionPreventsArtifactAbilityTargetingScion() {
        Permanent scion = addCreatureReady(player2, new YavimayaScion());
        harness.addToBattlefield(player1, new RingOfGix());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, scion.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts prevents combat damage from an artifact creature")
    void protectionPreventsArtifactCombatDamage() {
        Permanent scion = addCreatureReady(player1, new YavimayaScion());
        Permanent artifactAttacker = addCreatureReady(player2, new TickingGnomes());

        declareAttackers(player2, List.of(indexOf(player2, artifactAttacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, scion), indexOf(player2, artifactAttacker))));
        resolveCombat(player2);

        assertThat(scion.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Losing all abilities removes protection from artifacts")
    void losingAllAbilitiesRemovesProtectionFromArtifacts() {
        Permanent scion = addCreatureReady(player1, new YavimayaScion());
        scion.setLosesAllAbilitiesUntilEndOfTurn(true);
        Permanent artifactBlocker = addCreatureReady(player2, new TickingGnomes());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, scion, artifactBlocker)).isFalse();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, scion, new RingOfGix())).isFalse();

        declareAttackers(player1, List.of(indexOf(player1, scion)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, artifactBlocker), indexOf(player1, scion))));

        assertThat(artifactBlocker.isBlocking()).isTrue();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
