package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
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

@CardUsed({MammothUmbra.class, GrizzlyBears.class, DoomBlade.class, GlorySeeker.class, Naturalize.class})
class MammothUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3 and vigilance")
    void boostsEnchantedCreatureAndGrantsVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachUmbra(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from lethal damage and destroys the Aura")
    void savesFromLethalDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachUmbra(creature);
        creature.setMarkedDamage(5);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Mammoth Umbra");
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
        harness.assertInGraveyard(player1, "Mammoth Umbra");
    }


    @Test
    @DisplayName("Mammoth Umbra resolves attached to an opponent's creature and only boosts that creature")
    void resolvesOnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GlorySeeker());
        Permanent other = addCreatureReady(player1, new GlorySeeker());
        harness.setHand(player1, List.of(new MammothUmbra()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Mammoth Umbra");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance lets the enchanted creature attack without tapping")
    void attacksWithoutTapping() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        attachUmbra(creature);

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Destroying the Aura removes its boost and vigilance without destroying the creature")
    void destroyingAuraRemovesBonuses() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        Permanent aura = attachUmbra(creature);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, aura.getId());

        harness.assertOnBattlefield(player1, "Glory Seeker");
        harness.assertInGraveyard(player1, "Mammoth Umbra");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Umbra armor clears lethal and deathtouch damage without removing the creature from combat")
    void savesAttackerFromLethalDeathtouchDamage() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        attachUmbra(creature);
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        creature.setMarkedDamage(5);
        creature.setDamagedByDeathtouch(true);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Glory Seeker");
        harness.assertInGraveyard(player1, "Mammoth Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isDamagedByDeathtouch()).isFalse();
        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Umbra armor does not save a creature with zero toughness")
    void doesNotSaveFromZeroToughness() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        attachUmbra(creature);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 5);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Glory Seeker");
        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertInGraveyard(player1, "Mammoth Umbra");
    }

    @Test
    @DisplayName("Umbra armor preserves a tapped creature's state and protects it only once")
    void protectsTappedCreatureOnlyOnce() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        attachUmbra(creature);
        creature.tap();
        creature.setMarkedDamage(5);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Glory Seeker");
        harness.assertInGraveyard(player1, "Mammoth Umbra");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getMarkedDamage()).isZero();
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Glory Seeker");
        harness.assertInGraveyard(player1, "Glory Seeker");
    }

    private Permanent attachUmbra(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MammothUmbra());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
