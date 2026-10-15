package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderUmbra.class, GrizzlyBears.class, DoomBlade.class})
class SpiderUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and reach")
    void boostsEnchantedCreatureAndGrantsReach() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from lethal damage and destroys the Aura")
    void savesFromLethalDamage() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = attachUmbra(creature);
        creature.setMarkedDamage(3);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spider Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
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
        harness.assertInGraveyard(player1, "Spider Umbra");
    }

    @Test
    @DisplayName("Spider Umbra resolves on an opponent's creature and protects it only once")
    void enchantsAndProtectsOpponentsCreature() {
        Permanent creature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new SpiderUmbra()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Spider Umbra").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();

        creature.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spider Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();

        creature.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Umbra armor does not save a creature with zero toughness")
    void doesNotProtectZeroToughness() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spider Umbra");
    }

    @Test
    @DisplayName("Umbra armor clears lethal deathtouch damage without regenerating the creature")
    void clearsDeathtouchAndPreservesCombatStatus() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);
        creature.setCantRegenerateThisTurn(true);
        creature.setAttacking(true);
        creature.setMarkedDamage(3);
        creature.setDamagedByDeathtouch(true);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spider Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isDamagedByDeathtouch()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.isAttacking()).isTrue();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent attachUmbra(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpiderUmbra());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
