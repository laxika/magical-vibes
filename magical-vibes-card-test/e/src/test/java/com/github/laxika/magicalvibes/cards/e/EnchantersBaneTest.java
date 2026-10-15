package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.u.UrzasSaga;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnchantersBane.class, BadMoon.class, UrzasSaga.class})
class EnchantersBaneTest extends BaseCardTest {

    @Test
    void targetControllerMaySacrificeTheEnchantment() {
        harness.addToBattlefield(player1, new EnchantersBane());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BadMoon());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        moveToEndStep(player1);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void targetControllerTakesDamageEqualToManaValueWhenTheyDecline() {
        harness.addToBattlefield(player1, new EnchantersBane());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BadMoon());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        moveToEndStep(player1);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void doesNotTriggerDuringAnOpponentsEndStep() {
        harness.addToBattlefield(player1, new EnchantersBane());
        harness.addToBattlefield(player2, new BadMoon());

        moveToEndStep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mustChooseAnEnchantmentEvenWhenOnlyEnchantersBaneIsPresent() {
        Permanent bane = harness.addToBattlefieldAndReturn(player1, new EnchantersBane());

        moveToEndStep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(bane.getId());
    }

    @Test
    void canSacrificeItselfInsteadOfTakingDamage() {
        Permanent bane = harness.addToBattlefieldAndReturn(player1, new EnchantersBane());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        moveToEndStep(player1);
        harness.handlePermanentChosen(player1, bane.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bane);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bane.getCard());
    }

    @Test
    void dealsDamageToItsOwnControllerWhenTheyDeclineToSacrificeIt() {
        Permanent bane = harness.addToBattlefieldAndReturn(player1, new EnchantersBane());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        moveToEndStep(player1);
        harness.handlePermanentChosen(player1, bane.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bane);
    }

    @Test
    void canTargetAndSacrificeAnEnchantmentLand() {
        harness.addToBattlefield(player1, new EnchantersBane());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UrzasSaga());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        moveToEndStep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    private void moveToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }
}
