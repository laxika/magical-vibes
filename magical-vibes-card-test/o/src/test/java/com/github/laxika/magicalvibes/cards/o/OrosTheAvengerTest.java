package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.Calciderm;
import com.github.laxika.magicalvibes.cards.c.CitanulWoodreaders;
import com.github.laxika.magicalvibes.cards.g.GiantDustwasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrosTheAvenger.class, Calciderm.class, CitanulWoodreaders.class, GiantDustwasp.class})
class OrosTheAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{W} deals 3 damage to each nonwhite creature")
    void payingManaDamagesEachNonwhiteCreature() {
        harness.setLife(player2, 20);
        Permanent oros = addCreatureReady(player1, new OrosTheAvenger());
        oros.setAttacking(true);
        Permanent ownDustwasp = addCreatureReady(player1, new GiantDustwasp());
        Permanent opposingDustwasp = addCreatureReady(player2, new GiantDustwasp());
        Permanent survivingNonwhiteCreature = addCreatureReady(player2, new CitanulWoodreaders());
        Permanent whiteCreature = addCreatureReady(player2, new Calciderm());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oros).doesNotContain(ownDustwasp);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(opposingDustwasp)
                .contains(survivingNonwhiteCreature, whiteCreature);
        assertThat(survivingNonwhiteCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(whiteCreature.getMarkedDamage()).isZero();
        assertThat(oros.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining to pay {2}{W} does not deal the additional damage")
    void decliningManaPaymentDoesNothing() {
        harness.setLife(player2, 20);
        Permanent oros = addCreatureReady(player1, new OrosTheAvenger());
        oros.setAttacking(true);
        Permanent ownDustwasp = addCreatureReady(player1, new GiantDustwasp());
        Permanent opposingDustwasp = addCreatureReady(player2, new GiantDustwasp());

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oros, ownDustwasp);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingDustwasp);
    }

    @Test
    @DisplayName("The additional damage cannot be paid with three colorless mana")
    void cannotPayAdditionalDamageWithoutWhiteMana() {
        harness.setLife(player2, 20);
        Permanent oros = addCreatureReady(player1, new OrosTheAvenger());
        oros.setAttacking(true);
        Permanent nonwhiteCreature = addCreatureReady(player2, new CitanulWoodreaders());

        resolveCombat();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonwhiteCreature);
        assertThat(nonwhiteCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage dealt to a blocker does not trigger Oros's ability")
    void blockedCombatDamageDoesNotTriggerAbility() {
        harness.setLife(player2, 20);
        Permanent oros = addCreatureReady(player1, new OrosTheAvenger());
        Permanent blocker = addCreatureReady(player2, new GiantDustwasp());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oros);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(oros.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The combat damage trigger still deals damage after Oros leaves the battlefield")
    void triggerResolvesAfterOrosLeavesBattlefield() {
        harness.setLife(player2, 20);
        Permanent oros = addCreatureReady(player1, new OrosTheAvenger());
        oros.setAttacking(true);
        Permanent nonwhiteCreature = addCreatureReady(player2, new CitanulWoodreaders());
        Permanent whiteCreature = addCreatureReady(player2, new Calciderm());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, oros));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(oros);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonwhiteCreature, whiteCreature);
        assertThat(nonwhiteCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(whiteCreature.getMarkedDamage()).isZero();
        harness.assertLife(player2, 14);
    }
}
