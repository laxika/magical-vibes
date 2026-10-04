package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.cards.d.DawnElemental;
import com.github.laxika.magicalvibes.cards.e.ElvishAberration;
import com.github.laxika.magicalvibes.cards.s.SiegeGangCommander;
import com.github.laxika.magicalvibes.cards.u.Upwelling;
import com.github.laxika.magicalvibes.cards.w.WipeClean;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuiltyConscience.class, SiegeGangCommander.class, GoblinBrigand.class,
        ElvishAberration.class, Upwelling.class, WipeClean.class, DawnElemental.class, AuraGraft.class})
class GuiltyConscienceTest extends BaseCardTest {

    @Test
    @DisplayName("Guilty Conscience deals the damage dealt by the enchanted creature back to it")
    void dealsDamageEqualToEnchantedCreaturesDamage() {
        Permanent creature = addCreatureReady(player2, new SiegeGangCommander());
        Permanent goblin = addCreatureReady(player2, new GoblinBrigand());

        castGuiltyConscience(creature);

        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player2, ManaColor.RED, 2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.handlePermanentChosen(player2, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore - 2);
        harness.assertInGraveyard(player2, "Siege-Gang Commander");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A queued reflection still resolves after Guilty Conscience leaves the battlefield")
    void queuedReflectionResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player2, new SiegeGangCommander());
        Permanent goblin = addCreatureReady(player2, new GoblinBrigand());

        castGuiltyConscience(creature);
        Permanent aura = findPermanent(player1, "Guilty Conscience");

        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player2, ManaColor.RED, 2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.handlePermanentChosen(player2, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new WipeClean()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore - 2);
        harness.assertInGraveyard(player2, "Siege-Gang Commander");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Guilty Conscience reflects noncombat damage dealt to a permanent")
    void reflectsNoncombatDamageDealtToPermanent() {
        Permanent creature = addCreatureReady(player2, new SiegeGangCommander());
        Permanent goblin = addCreatureReady(player2, new GoblinBrigand());
        Permanent target = addCreatureReady(player1, new ElvishAberration());

        castGuiltyConscience(creature);

        harness.addMana(player2, ManaColor.RED, 2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.handlePermanentChosen(player2, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Siege-Gang Commander");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Guilty Conscience can enchant only a creature")
    void cannotEnchantNonCreaturePermanent() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Upwelling());

        harness.setHand(player1, List.of(new GuiltyConscience()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Guilty Conscience reflects combat damage dealt by the enchanted creature")
    void reflectsCombatDamageDealtByEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new ElvishAberration());
        castGuiltyConscience(creature);
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A queued reflection damages the original creature after the Aura moves")
    void queuedReflectionDamagesOriginalCreatureAfterAuraMoves() {
        Permanent creature = addCreatureReady(player2, new SiegeGangCommander());
        Permanent goblin = addCreatureReady(player2, new GoblinBrigand());
        Permanent destination = addCreatureReady(player2, new ElvishAberration());
        castGuiltyConscience(creature);
        Permanent aura = findPermanent(player1, "Guilty Conscience");

        harness.addMana(player2, ManaColor.RED, 2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.handlePermanentChosen(player2, goblin.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new AuraGraft()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.handlePermanentChosen(player2, destination.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Siege-Gang Commander");
        assertThat(destination.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Guilty Conscience");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prevented damage does not trigger Guilty Conscience")
    void fullyPreventedDamageDoesNotTriggerReflection() {
        Permanent creature = addCreatureReady(player2, new SiegeGangCommander());
        Permanent goblin = addCreatureReady(player2, new GoblinBrigand());
        Permanent target = addCreatureReady(player1, new DawnElemental());
        castGuiltyConscience(creature);

        harness.addMana(player2, ManaColor.RED, 2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.handlePermanentChosen(player2, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Siege-Gang Commander");
    }

    @Test
    @DisplayName("Guilty Conscience can enchant your creature and its damage can be prevented")
    void reflectsOwnCreaturesDamageThroughPrevention() {
        Permanent creature = addCreatureReady(player1, new DawnElemental());
        castGuiltyConscience(creature);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Dawn Elemental");
        assertThat(gd.stack).isEmpty();
    }

    private void castGuiltyConscience(Permanent creature) {
        harness.setHand(player1, List.of(new GuiltyConscience()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
