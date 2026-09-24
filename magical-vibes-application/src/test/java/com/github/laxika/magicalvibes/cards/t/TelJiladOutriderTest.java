package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AuriokSiegeSled;
import com.github.laxika.magicalvibes.cards.m.Memnarch;
import com.github.laxika.magicalvibes.cards.v.VulshokMorningstar;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelJiladOutrider.class, AuriokSiegeSled.class, TangleSpider.class, Memnarch.class,
        VulshokMorningstar.class})
class TelJiladOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from artifacts prevents blocking by an artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        Permanent outrider = addCreatureReady(player1, new TelJiladOutrider());
        outrider.setAttacking(true);
        Permanent artifactCreature = addCreatureReady(player2, new AuriokSiegeSled());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, artifactCreature), indexOf(player1, outrider)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by a non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        Permanent outrider = addCreatureReady(player1, new TelJiladOutrider());
        outrider.setAttacking(true);
        Permanent creature = addCreatureReady(player2, new TangleSpider());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, creature), indexOf(player1, outrider))));

        assertThat(creature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from artifacts prevents an artifact ability from targeting Tel-Jilad Outrider")
    void protectionPreventsArtifactAbilityTargetingOutrider() {
        Permanent outrider = addCreatureReady(player1, new TelJiladOutrider());
        addCreatureReady(player2, new Memnarch());
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, outrider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts prevents an artifact Equipment from equipping Tel-Jilad Outrider")
    void protectionPreventsArtifactEquipment() {
        Permanent outrider = addCreatureReady(player1, new TelJiladOutrider());
        Permanent morningstar = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, morningstar), null, outrider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts prevents combat damage from an artifact creature")
    void protectionPreventsArtifactCombatDamage() {
        Permanent outrider = addCreatureReady(player1, new TelJiladOutrider());
        Permanent artifactAttacker = addCreatureReady(player2, new AuriokSiegeSled());

        declareAttackers(player2, List.of(indexOf(player2, artifactAttacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, outrider), indexOf(player2, artifactAttacker))));
        resolveCombat(player2);

        assertThat(outrider.getMarkedDamage()).isZero();
        assertThat(artifactAttacker.getMarkedDamage()).isEqualTo(3);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
