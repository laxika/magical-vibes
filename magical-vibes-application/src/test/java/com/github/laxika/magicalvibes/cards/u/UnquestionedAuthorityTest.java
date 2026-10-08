package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.j.JeskaWarriorAdept;
import com.github.laxika.magicalvibes.cards.t.TrainedPronghorn;
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

@CardUsed({GiantWarthog.class, JeskaWarriorAdept.class, KrosanVerge.class, TrainedPronghorn.class, UnquestionedAuthority.class})
class UnquestionedAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Unquestioned Authority attaches it and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TrainedPronghorn());
        harness.setHand(player1, List.of(new UnquestionedAuthority()));
        harness.setLibrary(player1, List.of(new TrainedPronghorn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Unquestioned Authority")
                        && creature.getId().equals(permanent.getAttachedTo()));
        harness.assertInHand(player1, "Trained Pronghorn");
    }

    @Test
    @DisplayName("Enchanted creature can't be blocked by a creature")
    void creaturesCannotBlockEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new TrainedPronghorn());
        creature.setAttacking(true);
        enchant(creature);
        Permanent blocker = addCreatureReady(player2, new TrainedPronghorn());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Protection is lost when Unquestioned Authority leaves the battlefield")
    void protectionStopsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new TrainedPronghorn());
        Permanent aura = enchant(creature);
        Permanent attacker = addCreatureReady(player2, new TrainedPronghorn());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, creature, attacker)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, creature, attacker)).isFalse();
    }

    @Test
    @DisplayName("Protection applies to creature sources but not noncreature sources")
    void protectionOnlyAppliesToCreatureSources() {
        Permanent creature = addCreatureReady(player1, new TrainedPronghorn());
        enchant(creature);
        Permanent creatureSource = addCreatureReady(player2, new TrainedPronghorn());
        Permanent noncreatureSource = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, creature, creatureSource)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, creature, noncreatureSource)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        harness.setHand(player1, List.of(new UnquestionedAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent enchant(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnquestionedAuthority());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature takes no combat damage from a creature")
    void enchantedCreatureTakesNoCombatDamageFromCreature() {
        Permanent creature = addCreatureReady(player1, new TrainedPronghorn());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GiantWarthog());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        enchant(creature);

        resolveCombat();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Creature abilities cannot target the enchanted creature")
    void creatureAbilityCannotTargetEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TrainedPronghorn());
        enchant(creature);
        addCreatureReady(player1, new JeskaWarriorAdept());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Gaining protection makes a pending creature ability's target illegal")
    void protectionMakesPendingCreatureAbilityTargetIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TrainedPronghorn());
        addCreatureReady(player1, new JeskaWarriorAdept());
        harness.activateAbility(player1, 0, null, creature.getId());
        enchant(creature);

        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An Aura with a vanished target does not enter or draw a card")
    void vanishedTargetPreventsEntryAndDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TrainedPronghorn());
        harness.setHand(player1, List.of(new UnquestionedAuthority()));
        harness.setLibrary(player1, List.of(new GiantWarthog()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Unquestioned Authority")).isZero();
        harness.assertNotInHand(player1, "Giant Warthog");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Enchanting an opponent's creature draws for the Aura controller")
    void enchantingOpponentsCreatureDrawsForAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TrainedPronghorn());
        harness.setHand(player1, List.of(new UnquestionedAuthority()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GiantWarthog()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Unquestioned Authority").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertInHand(player1, "Giant Warthog");
        harness.assertNotInHand(player2, "Giant Warthog");
    }
}
