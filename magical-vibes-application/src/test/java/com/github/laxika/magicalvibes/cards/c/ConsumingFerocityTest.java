package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.Regeneration;
import com.github.laxika.magicalvibes.cards.w.WallOfCorpses;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        ConsumingFerocity.class,
        Disenchant.class,
        FemerefScouts.class,
        Forest.class,
        Regeneration.class,
        WallOfCorpses.class
})
class ConsumingFerocityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+0")
    void enchantedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FemerefScouts());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ConsumingFerocity());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Upkeep puts a +1/+0 counter on the enchanted creature without killing it")
    void upkeepPutsCounter() {
        Permanent creature = castOnFemerefScouts(player1);

        runUpkeep();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(1);
        // Base 1/4 + aura +1/+0 + one +1/+0 counter.
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Third counter makes the creature damage its controller for its power and die")
    void thirdCounterDestroysCreature() {
        Permanent creature = castOnFemerefScouts(player1);

        runUpkeep();
        runUpkeep();
        runUpkeep();

        // Power at the third trigger: base 1 + aura 1 + three +1/+0 counters = 5.
        harness.assertLife(player1, 15);
        harness.assertNotOnBattlefield(player1, "Femeref Scouts");
        harness.assertInGraveyard(player1, "Femeref Scouts");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Threshold damage is dealt to the enchanted creature's controller")
    void thresholdDamagesEnchantedCreatureController() {
        Permanent creature = castOnFemerefScouts(player2);

        runUpkeep();
        runUpkeep();
        runUpkeep();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Femeref Scouts");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Threshold destruction ignores a regeneration shield")
    void thresholdDestructionIgnoresRegenerationShield() {
        Permanent creature = castOnFemerefScouts(player1);
        Permanent regeneration = harness.addToBattlefieldAndReturn(player1, new Regeneration());
        regeneration.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        int regenerationIndex = gd.playerBattlefields.get(player1.getId()).indexOf(regeneration);
        harness.activateAbility(player1, regenerationIndex, null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);

        runUpkeep();
        runUpkeep();
        runUpkeep();

        harness.assertNotOnBattlefield(player1, "Femeref Scouts");
        harness.assertInGraveyard(player1, "Femeref Scouts");
    }

    @Test
    @DisplayName("Cannot enchant a Wall")
    void cannotEnchantWall() {
        harness.addToBattlefield(player2, new FemerefScouts());
        harness.addToBattlefield(player1, new WallOfCorpses());
        harness.setHand(player1, List.of(new ConsumingFerocity()));
        addConsumingFerocityMana(player1);

        Permanent wall = findPermanent(player1, "Wall of Corpses");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Wall creature");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        harness.addToBattlefield(player2, new FemerefScouts());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new ConsumingFerocity()));
        addConsumingFerocityMana(player1);

        Permanent forest = findPermanent(player1, "Forest");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Wall creature");
    }

    @Test
    @DisplayName("An upkeep trigger still resolves after the Aura leaves the battlefield")
    void upkeepTriggerUsesLastKnownAttachment() {
        Permanent creature = castOnFemerefScouts(player1);
        Permanent aura = findPermanent(player1, "Consuming Ferocity");

        advanceToUpkeep(player1);
        harness.passPriority(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Consuming Ferocity");
        harness.assertInGraveyard(player1, "Consuming Ferocity");

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(1);
    }

    private Permanent castOnFemerefScouts(Player creatureController) {
        Permanent creature = addCreatureReady(creatureController, new FemerefScouts());
        castConsumingFerocity(creature);
        return creature;
    }

    private void castConsumingFerocity(Permanent creature) {

        harness.setHand(player1, List.of(new ConsumingFerocity()));
        addConsumingFerocityMana(player1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void addConsumingFerocityMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private void runUpkeep() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
