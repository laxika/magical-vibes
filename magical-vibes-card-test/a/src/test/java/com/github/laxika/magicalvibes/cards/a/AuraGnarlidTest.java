package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuraGnarlid.class, GrizzlyBears.class, UnholyStrength.class})
class AuraGnarlidTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each Aura on the battlefield")
    void getsBoostForEachAuraOnBattlefield() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new AuraGnarlid());
        Permanent ownHost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentHost = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addAura(player1, ownHost);
        addAura(player2, opponentHost);

        assertThat(gqs.getEffectivePower(gd, gnarlid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gnarlid)).isEqualTo(4);
    }

    @Test
    @DisplayName("The Aura boost updates when an Aura leaves the battlefield")
    void boostUpdatesWhenAuraLeavesBattlefield() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new AuraGnarlid());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = addAura(player1, host);

        assertThat(gqs.getEffectivePower(gd, gnarlid)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gnarlid)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, gnarlid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gnarlid)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be blocked by a creature with less power")
    void cannotBeBlockedByLowerPower() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new AuraGnarlid());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addAura(player1, host);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gnarlid.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(gnarlid);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("Can be blocked by a creature with equal power")
    void canBeBlockedByEqualPower() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new AuraGnarlid());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gnarlid.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(gnarlid);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Auras in hands and graveyards do not contribute to the boost")
    void ignoresAurasOutsideBattlefield() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new AuraGnarlid());
        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.setGraveyard(player2, List.of(new UnholyStrength()));

        assertThat(gqs.getEffectivePower(gd, gnarlid)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gnarlid)).isEqualTo(2);
    }

    @Test
    @DisplayName("An Aura enchanting the Gnarlid contributes both its own boost and the Aura count")
    void countsAuraEnchantingItself() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new AuraGnarlid());
        addAura(player1, gnarlid);

        assertThat(gqs.getEffectivePower(gd, gnarlid)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, gnarlid)).isEqualTo(4);
    }

    @Test
    @DisplayName("A blocker with greater effective power can block despite its lower printed power")
    void canBeBlockedByAuraBoostedCreature() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new AuraGnarlid());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addAura(player2, blocker);
        gnarlid.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(gnarlid);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAura(com.github.laxika.magicalvibes.model.Player player, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, new UnholyStrength());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
