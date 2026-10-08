package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.h.HeartOfRamos;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulChanneling.class, FreshVolunteers.class, HeartOfRamos.class, Disenchant.class})
class SoulChannelingTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life regenerates the enchanted creature")
    void regeneratesEnchantedCreature() {
        Permanent aura = enchantCreature();
        Permanent creature = enchantedCreature(aura);

        harness.activateAbility(player1, indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can enchant and regenerate an opponent's creature")
    void canEnchantAndRegenerateOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new SoulChanneling()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Soul Channeling");
        harness.activateAbility(player1, indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot pay the regeneration cost without enough life")
    void cannotPayRegenerationCostWithoutEnoughLife() {
        Permanent aura = enchantCreature();
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HeartOfRamos());
        harness.setHand(player1, List.of(new SoulChanneling()));

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent enchantCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new SoulChanneling()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Soul Channeling");
    }

    @Test
    @DisplayName("Life is paid on activation before the regeneration shield resolves")
    void paysLifeBeforeResolution() {
        Permanent aura = enchantCreature();
        Permanent creature = enchantedCreature(aura);

        harness.activateAbility(player1, indexOf(aura), null, null);

        harness.assertLife(player1, 18);
        assertThat(creature.getRegenerationShield()).isZero();

        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations create separate shields without tapping")
    void repeatedActivationsCreateSeparateShields() {
        Permanent aura = enchantCreature();
        Permanent creature = enchantedCreature(aura);

        harness.activateAbility(player1, indexOf(aura), null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(aura), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(creature.getRegenerationShield()).isEqualTo(2);
        assertThat(creature.isTapped()).isFalse();
        assertThat(aura.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activated ability still shields the creature after the Aura is destroyed")
    void abilityResolvesAfterAuraIsDestroyed() {
        Permanent aura = enchantCreature();
        Permanent creature = enchantedCreature(aura);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player1, indexOf(aura), null, null);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Soul Channeling");
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration saves the enchanted creature from lethal combat damage")
    void regenerationSavesCreatureFromLethalCombatDamage() {
        Permanent aura = enchantCreature();
        Permanent creature = enchantedCreature(aura);
        harness.activateAbility(player1, indexOf(aura), null, null);
        harness.passBothPriorities();

        creature.setBlocking(true);
        creature.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new FreshVolunteers());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, aura);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isBlocking()).isFalse();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    private Permanent enchantedCreature(Permanent aura) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(aura.getAttachedTo()))
                .findFirst()
                .orElseThrow();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
