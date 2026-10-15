package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyrBattlesphere;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuddenDemise.class, FugitiveWizard.class, GrizzlyBears.class,
        BalefulStrix.class, MyrBattlesphere.class})
class SuddenDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage only to creatures of the chosen color")
    void damagesOnlyCreaturesOfChosenColor() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuddenDemise()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 1);

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

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(wizard.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
    }

    @ParameterizedTest
    @ValueSource(strings = {"BLUE", "BLACK"})
    @DisplayName("Either of a multicolored creature's colors matches, and colorless creatures are spared")
    void damagesMulticoloredCreaturesButNotColorlessCreatures(String color) {
        harness.addToBattlefield(player1, new BalefulStrix());
        harness.addToBattlefield(player2, new BalefulStrix());
        Permanent battlesphere = harness.addToBattlefieldAndReturn(player2, new MyrBattlesphere());
        harness.setHand(player1, List.of(new SuddenDemise()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3);
        harness.handleListChoice(player1, color);

        harness.assertInGraveyard(player1, "Baleful Strix");
        harness.assertInGraveyard(player2, "Baleful Strix");
        harness.assertOnBattlefield(player2, "Myr Battlesphere");
        assertThat(battlesphere.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each successive spell chooses its own color and uses its own X value")
    void successiveSpellsChooseIndependently() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuddenDemise(), new SuddenDemise()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleListChoice(player1, "GREEN");

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Fugitive Wizard");

        harness.castAndResolveSorcery(player1, 0, 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"WHITE", "RED", "GREEN"})
    @DisplayName("A color absent from the battlefield can be chosen without damaging creatures")
    void canChooseColorWithNoMatchingCreatures(String color) {
        Permanent strix = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        harness.setHand(player1, List.of(new SuddenDemise()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 4);
        harness.handleListChoice(player1, color);

        harness.assertOnBattlefield(player2, "Baleful Strix");
        assertThat(strix.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Sudden Demise");
    }
}
