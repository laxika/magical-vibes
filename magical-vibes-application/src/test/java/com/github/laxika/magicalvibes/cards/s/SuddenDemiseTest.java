package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuddenDemise.class, FugitiveWizard.class, GrizzlyBears.class})
class SuddenDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage only to creatures of the chosen color")
    void damagesOnlyCreaturesOfChosenColor() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuddenDemise()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("X=0 still prompts for a color and deals no damage")
    void zeroDamageStillChoosesColor() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new SuddenDemise()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(wizard.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
    }
}
