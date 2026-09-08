package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.cards.w.WarlordsAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TreefolkMysticTest extends BaseCardTest {

    @Test
    @DisplayName("When Treefolk Mystic blocks, it destroys all Auras attached to the attacker")
    void blocksDestroysAllAurasAttachedToAttacker() {
        Permanent mystic = addReadyMystic(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addAttachedAura(player1, new HolyStrength(), attacker);
        addAttachedAura(player1, new UnholyStrength(), attacker);
        addAttachedEquipment(player1, attacker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Holy Strength")
                        || p.getCard().getName().equals("Unholy Strength"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Holy Strength", "Unholy Strength");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Warlord's Axe"));
    }

    @Test
    @DisplayName("When Treefolk Mystic becomes blocked, it destroys all Auras attached to the blocker")
    void becomesBlockedDestroysAllAurasAttachedToBlocker() {
        Permanent mystic = addReadyMystic(player1);
        mystic.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addAttachedAura(player2, new HolyStrength(), blocker);
        addAttachedAura(player2, new UnholyStrength(), blocker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Holy Strength")
                        || p.getCard().getName().equals("Unholy Strength"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Holy Strength", "Unholy Strength");
    }

    private Permanent addReadyMystic(Player player) {
        Permanent mystic = new Permanent(new TreefolkMystic());
        mystic.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(mystic);
        return mystic;
    }

    private void addAttachedAura(Player player,
                                 Card auraCard,
                                 Permanent creature) {
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
    }

    private void addAttachedEquipment(Player player, Permanent creature) {
        Permanent equipment = new Permanent(new WarlordsAxe());
        equipment.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player.getId()).add(equipment);
    }
}
