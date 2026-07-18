package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpiritShackleTest extends BaseCardTest {

    // ===== Casting and targeting =====

    @Test
    @DisplayName("Can cast Spirit Shackle targeting a creature")
    void canTargetCreature() {
        harness.addToBattlefield(player1, new GiantSpider());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new SpiritShackle()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot cast Spirit Shackle targeting a non-creature permanent")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new GiantSpider()); // valid target so spell is playable
        harness.addToBattlefield(player1, new Mountain());
        Permanent land = findPermanent(player1, "Mountain");
        harness.setHand(player1, List.of(new SpiritShackle()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Tap trigger: -0/-2 counter on the enchanted creature =====

    @Test
    @DisplayName("Attacking (tapping) the enchanted creature puts a -0/-2 counter on it")
    void tappingEnchantedCreaturePutsCounter() {
        Permanent spider = new Permanent(new GiantSpider()); // 2/4
        spider.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(spider);
        attachShackle(player1, spider);

        assertThat(spider.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();

        declareAttack(player1, 0);
        harness.passBothPriorities(); // resolve the becomes-tapped trigger

        assertThat(spider.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2); // 4 base - 2
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2); // power unchanged
    }

    @Test
    @DisplayName("An un-enchanted creature becoming tapped gets no counter")
    void unenchantedCreatureGetsNoCounter() {
        Permanent spider = new Permanent(new GiantSpider());
        spider.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(spider);

        declareAttack(player1, 0);
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
    }

    // ===== Helpers =====

    private void attachShackle(Player owner, Permanent creature) {
        Permanent aura = new Permanent(new SpiritShackle());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(owner.getId()).add(aura);
    }

    private void declareAttack(Player attacker, int creatureIndex) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, attacker, List.of(creatureIndex));
    }
}
