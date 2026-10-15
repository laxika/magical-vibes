package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LagacLizard;
import com.github.laxika.magicalvibes.cards.v.Vendetta;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HyenaUmbra.class, GrizzlyBears.class, DoomBlade.class, LagacLizard.class,
        Vendetta.class, Demystify.class})
class HyenaUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and first strike")
    void boostsEnchantedCreatureAndGrantsFirstStrike() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from lethal damage and destroys the Aura")
    void savesFromLethalDamage() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = attachUmbra(creature);
        creature.setMarkedDamage(3);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hyena Umbra");
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
        harness.assertInGraveyard(player1, "Hyena Umbra");
    }

    @Test
    @DisplayName("Can enchant and protect an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new LagacLizard());
        Permanent other = addCreatureReady(player1, new LagacLizard());
        harness.setHand(player1, List.of(new HyenaUmbra()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Hyena Umbra");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();

        creature.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Lagac Lizard");
        harness.assertInGraveyard(player1, "Hyena Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Umbra armor works even when destruction forbids regeneration")
    void savesWhenRegenerationIsForbidden() {
        Permanent creature = addCreatureReady(player1, new LagacLizard());
        attachUmbra(creature);
        creature.setMarkedDamage(2);
        harness.setHand(player2, List.of(new Vendetta()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Lagac Lizard");
        harness.assertInGraveyard(player1, "Hyena Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Umbra armor preserves tapping and combat participation")
    void preservesCombatState() {
        Permanent creature = addCreatureReady(player1, new LagacLizard());
        attachUmbra(creature);
        creature.tap();
        creature.setAttacking(true);
        creature.setMarkedDamage(4);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Lagac Lizard");
        harness.assertInGraveyard(player1, "Hyena Umbra");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("One umbra armor replacement handles lethal damage and deathtouch together")
    void savesFromLethalDamageAndDeathtouch() {
        Permanent creature = addCreatureReady(player1, new LagacLizard());
        attachUmbra(creature);
        creature.setMarkedDamage(4);
        creature.setDamagedByDeathtouch(true);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Lagac Lizard");
        harness.assertInGraveyard(player1, "Hyena Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isDamagedByDeathtouch()).isFalse();
    }

    @Test
    @DisplayName("Umbra armor cannot replace death from zero toughness")
    void doesNotSaveFromZeroToughness() {
        Permanent creature = addCreatureReady(player1, new LagacLizard());
        attachUmbra(creature);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Lagac Lizard");
        harness.assertInGraveyard(player1, "Lagac Lizard");
        harness.assertInGraveyard(player1, "Hyena Umbra");
    }

    @Test
    @DisplayName("Destroying the Aura directly removes its bonuses without destroying the creature")
    void destroyingAuraRemovesBonuses() {
        Permanent creature = addCreatureReady(player1, new LagacLizard());
        Permanent aura = attachUmbra(creature);
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, aura.getId());

        harness.assertOnBattlefield(player1, "Lagac Lizard");
        harness.assertInGraveyard(player1, "Hyena Umbra");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The enchanted creature's controller chooses exactly one of multiple Umbras")
    void creatureControllerChoosesWhichUmbraIsDestroyed() {
        Permanent creature = addCreatureReady(player2, new LagacLizard());
        Permanent firstAura = attachUmbra(creature);
        Permanent secondAura = attachUmbra(creature);
        creature.setMarkedDamage(5);

        harness.runStateBasedActions();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstAura, secondAura);
        harness.handleListChoice(player2, gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)
                .options().getLast());

        harness.assertOnBattlefield(player2, "Lagac Lizard");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstAura).doesNotContain(secondAura);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondAura.getCard())
                .doesNotContain(firstAura.getCard());
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An indestructible creature does not consume umbra armor on lethal damage")
    void indestructibleCreatureKeepsAura() {
        Permanent creature = addCreatureReady(player1, new LagacLizard());
        Permanent aura = attachUmbra(creature);
        creature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        creature.setMarkedDamage(4);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Lagac Lizard");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        harness.assertNotInGraveyard(player1, "Hyena Umbra");
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent attachUmbra(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HyenaUmbra());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
