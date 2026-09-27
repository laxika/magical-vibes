package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnchantersBane.class, BadMoon.class})
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

    private void moveToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }
}
