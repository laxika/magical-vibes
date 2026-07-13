package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PestilenceTest extends BaseCardTest {

    @Test
    @DisplayName("{B}: deals 1 damage to each creature and each player")
    void activatedAbilityDealsOneDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Pestilence());
        harness.addToBattlefield(player2, new FugitiveWizard()); // 1/1 dies
        harness.addToBattlefield(player2, new GrizzlyBears());    // 2/2 survives
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fugitive Wizard"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrifices itself at end step when no creatures are on the battlefield")
    void sacrificesAtEndStepWhenNoCreatures() {
        GameData gd = harness.getGameData();
        Permanent pestilence = new Permanent(new Pestilence());
        gd.playerBattlefields.get(player1.getId()).add(pestilence);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pestilence"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pestilence"));
    }

    @Test
    @DisplayName("Does not sacrifice itself while a creature is on the battlefield")
    void doesNotSacrificeWhenCreaturePresent() {
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Pestilence()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // The intervening-if fails (a creature is present), so no sacrifice trigger is put on the
        // stack and Pestilence survives past the end step.
        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Pestilence"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pestilence"));
    }
}
