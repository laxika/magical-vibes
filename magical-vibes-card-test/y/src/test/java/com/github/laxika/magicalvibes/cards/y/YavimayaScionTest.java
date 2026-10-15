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

        declareAttackersAndPrepareBlockers(player2, List.of(indexOf(player2, artifactAttacker)));
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

        declareAttackersAndPrepareBlockers(player1, List.of(indexOf(player1, scion)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, artifactBlocker), indexOf(player1, scion))));

        assertThat(artifactBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from artifacts also prevents targeting by a friendly artifact")
    void protectionPreventsFriendlyArtifactTargeting() {
        harness.addToBattlefield(player1, new RingOfGix());
        Permanent scion = addCreatureReady(player1, new YavimayaScion());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, scion.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(scion.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Losing protection allows an artifact ability to target and tap the creature")
    void losingProtectionAllowsArtifactTargeting() {
        Permanent scion = addCreatureReady(player2, new YavimayaScion());
        scion.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.addToBattlefield(player1, new RingOfGix());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, scion.getId());
        harness.passBothPriorities();

        assertThat(scion.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Protection from artifacts does not prevent lethal non-artifact combat damage")
    void nonArtifactCombatDamageIsNotPrevented() {
        Permanent scion = addCreatureReady(player1, new YavimayaScion());
        Permanent attacker = addCreatureReady(player2, new GiantCockroach());

        declareAttackersAndPrepareBlockers(player2, List.of(indexOf(player2, attacker)));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, scion), indexOf(player2, attacker))));
        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Yavimaya Scion");
        harness.assertNotOnBattlefield(player1, "Yavimaya Scion");
    }

    @Test
    @DisplayName("Regaining protection makes sacrificed Ticking Gnomes' target illegal on resolution")
    void regainedProtectionInvalidatesSacrificedArtifactTarget() {
        Permanent scion = addCreatureReady(player2, new YavimayaScion());
        scion.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.addToBattlefield(player1, new TickingGnomes());

        harness.activateAbility(player1, 0, null, scion.getId());
        harness.assertInGraveyard(player1, "Ticking Gnomes");
        scion.setLosesAllAbilitiesUntilEndOfTurn(false);
        harness.passBothPriorities();

        assertThat(scion.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Yavimaya Scion");
        assertThat(gd.stack).isEmpty();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
