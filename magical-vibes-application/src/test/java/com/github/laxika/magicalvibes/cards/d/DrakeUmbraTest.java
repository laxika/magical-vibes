package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrakeUmbra.class, GrizzlyBears.class, DoomBlade.class, Naturalize.class})
class DrakeUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3 and flying")
    void boostsEnchantedCreatureAndGrantsFlying() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachUmbra(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from lethal damage and destroys the Aura")
    void savesFromLethalDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachUmbra(creature);
        creature.setMarkedDamage(5);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Drake Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from a destroy effect")
    void savesFromDestroyEffect() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachUmbra(creature);

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Drake Umbra");
    }

    @Test
    @DisplayName("Drake Umbra can enchant an opposing creature when cast")
    void castsOntoOpposingCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DrakeUmbra()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Drake Umbra");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Destroying Drake Umbra removes its bonuses without destroying the creature")
    void destroyingAuraRemovesBonuses() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachUmbra(creature);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, aura.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Drake Umbra");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Umbra armor clears lethal damage and deathtouch together without tapping the creature")
    void clearsLethalDamageAndDeathtouch() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachUmbra(creature);
        creature.setMarkedDamage(5);
        creature.setDamagedByDeathtouch(true);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Drake Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isDamagedByDeathtouch()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Umbra armor does not save a creature with zero toughness")
    void zeroToughnessDoesNotUseArmor() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachUmbra(creature);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 5);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Drake Umbra");
    }

    @Test
    @DisplayName("Umbra armor works when regeneration is prohibited and preserves tapped status")
    void armorIsNotRegeneration() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachUmbra(creature);
        creature.setTapped(true);
        creature.setCantRegenerateThisTurn(true);
        creature.setMarkedDamage(5);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Drake Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The creature's controller chooses which armor Aura is destroyed")
    void multipleArmorAurasRequireControllerChoice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstAura = attachUmbra(creature);
        Permanent secondAura = attachUmbra(creature);
        creature.setMarkedDamage(8);

        harness.runStateBasedActions();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, firstAura, secondAura);
    }

    private Permanent attachUmbra(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DrakeUmbra());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
