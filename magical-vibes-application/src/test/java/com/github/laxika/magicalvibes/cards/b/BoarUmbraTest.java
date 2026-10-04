package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FleshToDust;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoarUmbra.class, GrizzlyBears.class, DoomBlade.class, FleshToDust.class})
class BoarUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void boostsEnchantedCreature() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from lethal damage and destroys the Aura")
    void savesFromLethalDamage() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = attachUmbra(creature);
        creature.setMarkedDamage(5);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Boar Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from a destroy effect")
    void savesFromDestroyEffect() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Boar Umbra");
    }

    @Test
    @DisplayName("Umbra armor works even when the destroy effect prohibits regeneration")
    void worksAgainstDestroyEffectThatProhibitsRegeneration() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);

        harness.setHand(player2, List.of(new FleshToDust()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Boar Umbra");
    }

    @Test
    @DisplayName("Boar Umbra can resolve attached to an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent creature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new BoarUmbra()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Boar Umbra").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        creature.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Boar Umbra");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Umbra armor does not protect a creature with zero toughness")
    void doesNotProtectZeroToughness() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 5);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Boar Umbra");
    }

    @Test
    @DisplayName("Umbra armor preserves a tapped creature's combat status and clears deathtouch damage")
    void preservesCombatStatusAndClearsDeathtouch() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);
        creature.tap();
        creature.setAttacking(true);
        creature.setMarkedDamage(1);
        creature.setDamagedByDeathtouch(true);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Boar Umbra");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isDamagedByDeathtouch()).isFalse();
    }

    @Test
    @DisplayName("After the Aura is consumed, subsequent lethal damage destroys the creature")
    void doesNotProtectSubsequentDestruction() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);
        creature.setMarkedDamage(5);
        harness.runStateBasedActions();
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The enchanted creature's controller chooses which of multiple Umbras is destroyed")
    void controllerChoosesBetweenMultipleUmbras() {
        Permanent creature = addReadyCreature(player1);
        Permanent firstAura = attachUmbra(creature);
        Permanent secondAura = attachUmbra(creature);
        creature.setMarkedDamage(8);

        harness.runStateBasedActions();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, firstAura, secondAura);
    }

    @Test
    @DisplayName("The creature's controller may choose regeneration instead of consuming Boar Umbra")
    void controllerChoosesBetweenRegenerationAndUmbraArmor() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = attachUmbra(creature);
        creature.setRegenerationShield(1);
        creature.setMarkedDamage(5);

        harness.runStateBasedActions();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, aura);
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent attachUmbra(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BoarUmbra());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
