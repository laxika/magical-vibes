package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lure.class, AirElemental.class, GrizzlyBears.class, Mountain.class})
class LureTest extends BaseCardTest {

    

    @Test
    @DisplayName("All able creatures must block enchanted attacker")
    void allAbleCreaturesMustBlock() {
        Permanent enchantedAttacker = attackingCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(enchantedAttacker);
        Permanent lure = new Permanent(new Lure());
        lure.setAttachedTo(enchantedAttacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(lure);

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 2 blockers"));
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block by Lure")
    void tappedCreaturesNotForcedToBlock() {
        Permanent enchantedAttacker = attackingCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(enchantedAttacker);
        Permanent lure = new Permanent(new Lure());
        lure.setAttachedTo(enchantedAttacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(lure);

        Permanent untapped = addCreatureReady(player2, new GrizzlyBears());
        Permanent tapped = addCreatureReady(player2, new GrizzlyBears());
        tapped.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Creatures unable to block enchanted attacker are not forced by Lure")
    void unableBlockersNotForced() {
        Permanent enchantedAttacker = attackingCreature(new AirElemental());
        gd.playerBattlefields.get(player1.getId()).add(enchantedAttacker);
        Permanent lure = new Permanent(new Lure());
        lure.setAttachedTo(enchantedAttacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(lure);

        Permanent nonFlying = addCreatureReady(player2, new GrizzlyBears());
        Permanent flying = addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(nonFlying.isBlocking()).isFalse();
        assertThat(flying.isBlocking()).isTrue();
    }

    private Permanent attackingCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
        return permanent;
    }

    @Test
    @DisplayName("Casting Lure attaches it to a target creature")
    void castingLureAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Lure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Lure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
