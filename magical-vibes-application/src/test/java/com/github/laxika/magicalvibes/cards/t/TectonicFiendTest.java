package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TectonicFiend.class)
class TectonicFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Tectonic Fiend at its next upkeep")
    void decliningEchoSacrificesTectonicFiend() {
        castAndResolveTectonicFiend();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Tectonic Fiend");
        harness.assertInGraveyard(player1, "Tectonic Fiend");
    }

    @Test
    @DisplayName("Paying echo keeps Tectonic Fiend and echo does not trigger again")
    void payingEchoKeepsTectonicFiendAndIsOneShot() {
        castAndResolveTectonicFiend();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        addEchoMana();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Tectonic Fiend");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Tectonic Fiend");
    }

    @Test
    @DisplayName("Tectonic Fiend must attack each combat when able")
    void mustAttackWhenAble() {
        Permanent fiend = new Permanent(new TectonicFiend());
        fiend.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(fiend);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private void castAndResolveTectonicFiend() {
        harness.setHand(player1, List.of(new TectonicFiend()));
        addEchoMana();
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addEchoMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
