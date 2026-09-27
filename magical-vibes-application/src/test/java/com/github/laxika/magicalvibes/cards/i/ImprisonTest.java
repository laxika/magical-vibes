package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.p.PsionicEntity;
import com.github.laxika.magicalvibes.cards.s.SunastianFalconer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Imprison.class, PsionicEntity.class, SunastianFalconer.class, BarbaryApes.class})
class ImprisonTest extends BaseCardTest {

    @Test
    void paysToCounterTapAbilityWithoutDestroyingAura() {
        Permanent creature = addCreatureReady(player2, new PsionicEntity());
        Permanent aura = addAura(player1, creature);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void paysToCounterControllerTapAbilityWithoutDestroyingAura() {
        Permanent creature = addCreatureReady(player1, new PsionicEntity());
        Permanent aura = addAura(player1, creature);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void decliningToCounterDestroysAuraAndLetsAbilityResolve() {
        Permanent creature = addCreatureReady(player2, new PsionicEntity());
        Permanent aura = addAura(player1, creature);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void doesNotTriggerForManaAbility() {
        Permanent creature = addCreatureReady(player1, new SunastianFalconer());
        Permanent aura = addAura(player1, creature);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    void paysToTapAndRemoveAttackingEnchantedCreatureFromCombat() {
        Permanent creature = addCreatureReady(player1, new BarbaryApes());
        Permanent aura = addAura(player1, creature);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isAttacking()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    void paysToTapAndRemoveBlockingEnchantedCreatureFromCombat() {
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());
        Permanent attacker = addCreatureReady(player1, new BarbaryApes());
        attacker.setAttacking(true);
        Permanent aura = addAura(player1, blocker);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(attacker.getBlockingTargetIds()).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    void payingToRemoveSoleBlockerMakesAttackerUnblocked() {
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());
        Permanent attacker = addCreatureReady(player1, new BarbaryApes());
        attacker.setAttacking(true);
        addAura(player1, blocker);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void decliningToTapAndRemoveAttackingCreatureDestroysAura() {
        Permanent creature = addCreatureReady(player1, new BarbaryApes());
        Permanent aura = addAura(player1, creature);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura.getCard());
    }

    private Permanent addAura(com.github.laxika.magicalvibes.model.Player controller, Permanent creature) {
        Permanent aura = new Permanent(new Imprison());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
