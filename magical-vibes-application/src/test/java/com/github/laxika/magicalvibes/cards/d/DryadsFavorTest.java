package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DryadsFavor.class, RuneclawBear.class, Mountain.class, Forest.class})
class DryadsFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dryad's Favor puts it on the stack as enchantment spell")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new DryadsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(DryadsFavor.class);
    }

    @Test
    @DisplayName("Resolving Dryad's Favor attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new DryadsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dryad's Favor")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has forestwalk")
    void enchantedCreatureHasForestwalk() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent favorPerm = harness.addToBattlefieldAndReturn(player1, new DryadsFavor());
        favorPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Dryad's Favor does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent otherBears = addCreatureReady(player1, new RuneclawBear());

        Permanent favorPerm = harness.addToBattlefieldAndReturn(player1, new DryadsFavor());
        favorPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Creature loses forestwalk when Dryad's Favor is removed")
    void creatureLosesForestwalkWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent favorPerm = harness.addToBattlefieldAndReturn(player1, new DryadsFavor());
        favorPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FORESTWALK)).isTrue();

        // Remove the aura from the battlefield
        gd.playerBattlefields.get(player1.getId()).remove(favorPerm);
        gd.playerGraveyards.get(player1.getId()).add(favorPerm.getCard());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Dryad's Favor fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new DryadsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Dryad's Favor should go to graveyard (fizzle)
        harness.assertInGraveyard(player1, "Dryad's Favor");
        harness.assertNotOnBattlefield(player1, "Dryad's Favor");
    }

    @Test
    @DisplayName("Dryad's Favor goes to graveyard when enchanted creature dies")
    void goesToGraveyardWhenCreatureDies() {
        Permanent bearsPerm = addCreatureReady(player2, new RuneclawBear());

        Permanent favorPerm = harness.addToBattlefieldAndReturn(player2, new DryadsFavor());
        favorPerm.setAttachedTo(bearsPerm.getId());

        addCreatureReady(player1, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dryad's Favor");
        harness.assertInGraveyard(player2, "Dryad's Favor");
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new DryadsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
    @ParameterizedTest
    @CsvSource({"true, false, false", "false, false, true", "false, true, true"})
    @DisplayName("Only a Forest controlled by the defending player prevents blocking")
    void forestwalkDependsOnDefendingPlayersForest(boolean defenderHasForest,
                                                  boolean attackerHasForest,
                                                  boolean canBlock) {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        if (defenderHasForest) {
            harness.addToBattlefield(player2, new Forest());
        } else {
            harness.addToBattlefield(player2, new Mountain());
        }
        if (attackerHasForest) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new DryadsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        if (canBlock) {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            assertThat(blocker.isBlocking()).isTrue();
        } else {
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                    List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be blocked");
        }
    }

    @Test
    @DisplayName("Dryad's Favor can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new DryadsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dryad's Favor");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FORESTWALK)).isTrue();
    }
}
