package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilverquillSilencerTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, it records the chosen nonland card name")
    void choosesCardNameOnEnter() {
        harness.setHand(player1, List.of(new SilverquillSilencer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(findPermanent(player1, "Silverquill Silencer").getChosenName())
                .isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent casting the chosen spell causes life loss and a card draw")
    void opponentCastsChosenSpell() {
        addReadySilencer(player1, "Grizzly Bears");
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLife(player2, 10);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("An opponent casting a different spell does not trigger")
    void opponentCastsDifferentSpell() {
        addReadySilencer(player1, "Grizzly Bears");
        harness.setLife(player2, 10);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    private Permanent addReadySilencer(Player player, String chosenName) {
        Permanent perm = new Permanent(new SilverquillSilencer());
        perm.setChosenName(chosenName);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
