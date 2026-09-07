package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pyrohemia.class, FugitiveWizard.class, GrizzlyBears.class})
class PyrohemiaTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: deals 1 damage to each creature and each player")
    void activatedAbilityDealsOneDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Pyrohemia());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrifices itself at end step when no creatures are on the battlefield")
    void sacrificesAtEndStepWhenNoCreatures() {
        GameData gd = harness.getGameData();
        Permanent pyrohemia = new Permanent(new Pyrohemia());
        gd.playerBattlefields.get(player1.getId()).add(pyrohemia);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pyrohemia");
        harness.assertInGraveyard(player1, "Pyrohemia");
    }

    @Test
    @DisplayName("Does not sacrifice itself while a creature is on the battlefield")
    void doesNotSacrificeWhenCreaturePresent() {
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Pyrohemia()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Pyrohemia"));
        harness.assertOnBattlefield(player1, "Pyrohemia");
    }
}
