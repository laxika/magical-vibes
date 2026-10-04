package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HyenaUmbra;
import com.github.laxika.magicalvibes.cards.v.Vendetta;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElandUmbra.class, GrizzlyBears.class, DoomBlade.class, Vendetta.class, HyenaUmbra.class})
class ElandUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +0/+4")
    void boostsEnchantedCreature() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from lethal damage and destroys the Aura")
    void savesFromLethalDamage() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = attachUmbra(creature);
        creature.setMarkedDamage(6);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Eland Umbra");
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
        harness.assertInGraveyard(player1, "Eland Umbra");
    }

    @Test
    @DisplayName("Umbra armor works even when the destroy effect prohibits regeneration")
    void worksAgainstDestroyEffectThatProhibitsRegeneration() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);

        harness.setHand(player2, List.of(new Vendetta()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Eland Umbra");
    }

    @Test
    @DisplayName("The Aura can be cast on an opponent's creature and protects that creature")
    void enchantsOpponentsCreature() {
        Permanent creature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ElandUmbra()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Eland Umbra");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        creature.setMarkedDamage(6);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Eland Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Umbra armor does not untap the creature or remove it from combat")
    void preservesCombatState() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);
        creature.setTapped(true);
        creature.setAttacking(true);
        creature.setMarkedDamage(6);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Eland Umbra");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("One replacement handles simultaneous lethal damage and deathtouch")
    void savesFromLethalDamageAndDeathtouch() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);
        creature.setMarkedDamage(6);
        creature.setDamagedByDeathtouch(true);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Eland Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isDamagedByDeathtouch()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Umbra armor cannot save a creature with zero toughness")
    void doesNotSaveFromZeroToughness() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 6);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Eland Umbra");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The creature's controller chooses which umbra armor Aura is destroyed")
    void offersChoiceBetweenUmbraAuras() {
        Permanent creature = addReadyCreature(player1);
        Permanent eland = attachUmbra(creature);
        Permanent hyena = harness.addToBattlefieldAndReturn(player1, new HyenaUmbra());
        hyena.setAttachedTo(creature.getId());
        creature.setMarkedDamage(7);

        harness.runStateBasedActions();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, eland, hyena);
    }

    @Test
    @DisplayName("The creature's controller can choose regeneration instead of umbra armor")
    void offersChoiceBetweenRegenerationAndUmbraArmor() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = attachUmbra(creature);
        creature.setRegenerationShield(1);
        creature.setMarkedDamage(6);

        harness.runStateBasedActions();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, aura);
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent attachUmbra(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ElandUmbra());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
