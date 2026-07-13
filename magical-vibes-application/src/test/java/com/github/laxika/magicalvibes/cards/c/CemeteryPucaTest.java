package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CemeteryPucaTest extends BaseCardTest {

    private Permanent putPuca() {
        Permanent puca = new Permanent(new CemeteryPuca());
        gd.playerBattlefields.get(player1.getId()).add(puca);
        return puca;
    }

    @Test
    @DisplayName("Pays {1} to become a copy of a creature that died")
    void becomesCopyWhenPaid() {
        Permanent puca = putPuca();

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.addMana(player1, ManaColor.COLORLESS, 1); // for the {1}
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities(); // Shock resolves, Grizzly Bears dies, trigger fires

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // become-copy resolves

        assertThat(puca.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, puca)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, puca)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining to pay leaves it as Cemetery Puca")
    void staysWhenDeclined() {
        Permanent puca = putPuca();

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(puca.getCard().getName()).isEqualTo("Cemetery Puca");
    }

    @Test
    @DisplayName("Retains the copy ability — copies again when another creature dies (\"except it has this ability\")")
    void retainsAbilityAndCopiesAgain() {
        Permanent puca = putPuca();

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        Permanent runeclaw = new Permanent(new RuneclawBear());
        gd.playerBattlefields.get(player2.getId()).add(runeclaw);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        // First death: copy Grizzly Bears
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(puca.getCard().getName()).isEqualTo("Grizzly Bears");

        // Second death: the copy still has the trigger, so it fires again and copies Runeclaw Bear
        harness.castInstant(player1, 0, runeclaw.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(puca.getCard().getName()).isEqualTo("Runeclaw Bear");
    }
}
