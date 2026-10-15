package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FishliverOil.class, GrizzlyBears.class, Island.class})
class FishliverOilTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Fishliver Oil puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FishliverOil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Fishliver Oil attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FishliverOil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fishliver Oil")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has islandwalk")
    void enchantedCreatureHasIslandwalk() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent oilPerm = harness.addToBattlefieldAndReturn(player1, new FishliverOil());
        oilPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Islandwalk prevents blocking while defending player controls an Island")
    void islandwalkPreventsBlockingWithIsland() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent oilPerm = harness.addToBattlefieldAndReturn(player1, new FishliverOil());
        oilPerm.setAttachedTo(attacker.getId());

        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("islandwalk");
    }

    @Test
    @DisplayName("Islandwalk allows blocking when defending player controls no Island")
    void islandwalkAllowsBlockingWithoutIsland() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent oilPerm = harness.addToBattlefieldAndReturn(player1, new FishliverOil());
        oilPerm.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Creature loses islandwalk when Fishliver Oil is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent oilPerm = harness.addToBattlefieldAndReturn(player1, new FishliverOil());
        oilPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.ISLANDWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(oilPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Fishliver Oil does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());

        Permanent oilPerm = harness.addToBattlefieldAndReturn(player1, new FishliverOil());
        oilPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Fishliver Oil")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new FishliverOil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fishliver Oil can enchant an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FishliverOil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof FishliverOil)
                .findFirst().orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Fishliver Oil goes to the graveyard when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FishliverOil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Fishliver Oil");
        harness.assertInGraveyard(player1, "Fishliver Oil");
    }

    @Test
    @DisplayName("An Island controlled only by the attacker does not prevent blocking")
    void attackersIslandDoesNotPreventBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FishliverOil());
        aura.setAttachedTo(attacker.getId());
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
