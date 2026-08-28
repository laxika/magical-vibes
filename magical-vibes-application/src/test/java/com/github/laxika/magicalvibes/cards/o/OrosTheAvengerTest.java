package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrosTheAvenger.class, GrizzlyBears.class, SerraAngel.class})
class OrosTheAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{W} deals 3 damage to each nonwhite creature")
    void payingManaDamagesEachNonwhiteCreature() {
        harness.setLife(player2, 20);
        Permanent oros = addCreatureReady(player1, new OrosTheAvenger());
        oros.setAttacking(true);
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent whiteCreature = addCreatureReady(player2, new SerraAngel());

        resolveCombatToMayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oros).doesNotContain(ownBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingBear).contains(whiteCreature);
        assertThat(whiteCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining to pay {2}{W} does not deal the additional damage")
    void decliningManaPaymentDoesNothing() {
        harness.setLife(player2, 20);
        Permanent oros = addCreatureReady(player1, new OrosTheAvenger());
        oros.setAttacking(true);
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        resolveCombatToMayPrompt();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oros, ownBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingBear);
    }

    private void resolveCombatToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
