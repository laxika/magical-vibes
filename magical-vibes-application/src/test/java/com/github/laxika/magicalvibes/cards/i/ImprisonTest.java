package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Imprison.class, ZuranSpellcaster.class, LlanowarElves.class, GrizzlyBears.class})
class ImprisonTest extends BaseCardTest {

    @Test
    void paysToCounterTapAbilityWithoutDestroyingAura() {
        Permanent creature = addCreatureReady(player2, new ZuranSpellcaster());
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
    void decliningToCounterDestroysAuraAndLetsAbilityResolve() {
        Permanent creature = addCreatureReady(player2, new ZuranSpellcaster());
        Permanent aura = addAura(player1, creature);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void doesNotTriggerForManaAbility() {
        Permanent creature = addCreatureReady(player1, new LlanowarElves());
        Permanent aura = addAura(player1, creature);

        harness.tapPermanent(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    void paysToTapAndRemoveAttackingEnchantedCreatureFromCombat() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent aura = addAura(player1, blocker);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(attacker.getBlockingTargetIds()).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    private Permanent addAura(com.github.laxika.magicalvibes.model.Player controller, Permanent creature) {
        Permanent aura = new Permanent(new Imprison());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
