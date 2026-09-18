package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.s.SparkSpray;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.cards.t.TitanicBulvox;
import com.github.laxika.magicalvibes.cards.w.WipeClean;
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

@CardUsed({FrozenSolid.class, ScornfulEgotist.class, SparkSpray.class,
        TempleOfTheFalseGod.class, TitanicBulvox.class, WipeClean.class})
class FrozenSolidTest extends BaseCardTest {

    @Test
    @DisplayName("Frozen Solid attaches to the target creature")
    void attachesToTargetCreature() {
        Permanent creature = addCreatureReady(player2, new TitanicBulvox());
        harness.setHand(player1, List.of(new FrozenSolid()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Frozen Solid");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Frozen Solid prevents the enchanted creature from untapping")
    void preventsUntapping() {
        Permanent creature = addCreatureReady(player2, new TitanicBulvox());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FrozenSolid());
        aura.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Nonlethal damage to the enchanted creature destroys it")
    void damageDestroysEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new TitanicBulvox());
        Permanent aura = attachFrozenSolid(creature);

        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Damage to another creature does not trigger Frozen Solid")
    void damageToAnotherCreatureDoesNotTrigger() {
        Permanent enchanted = addCreatureReady(player2, new TitanicBulvox());
        Permanent other = addCreatureReady(player2, new ScornfulEgotist());
        Permanent aura = attachFrozenSolid(enchanted);

        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, other.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(other);
    }

    @Test
    @DisplayName("Combat damage to the enchanted creature destroys it")
    void combatDamageDestroysEnchantedCreature() {
        Permanent attacker = addCreatureReady(player1, new ScornfulEgotist());
        Permanent enchanted = addCreatureReady(player2, new TitanicBulvox());
        Permanent aura = attachFrozenSolid(enchanted);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("Fully prevented damage does not trigger Frozen Solid")
    void fullyPreventedDamageDoesNotTrigger() {
        Permanent enchanted = addCreatureReady(player2, new TitanicBulvox());
        enchanted.setDamagePreventionShield(1);
        Permanent aura = attachFrozenSolid(enchanted);

        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, enchanted.getId());
        resolveAllTriggers();

        assertThat(enchanted.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("A pending damage trigger still destroys the creature if Frozen Solid leaves")
    void pendingTriggerSurvivesAuraLeaving() {
        Permanent enchanted = addCreatureReady(player2, new TitanicBulvox());
        Permanent aura = attachFrozenSolid(enchanted);

        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Frozen Solid"));

        harness.setHand(player1, List.of(new WipeClean()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Frozen Solid can target only a creature")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TempleOfTheFalseGod());
        harness.setHand(player1, List.of(new FrozenSolid()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachFrozenSolid(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FrozenSolid());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
