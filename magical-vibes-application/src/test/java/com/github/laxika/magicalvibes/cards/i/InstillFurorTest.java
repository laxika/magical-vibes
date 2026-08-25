package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InstillFuror.class, GrizzlyBears.class})
class InstillFurorTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature is sacrificed at its controller's end step if it did not attack")
    void sacrificesNonAttackerAtControllerEndStep() {
        Permanent creature = addCreature(player1);
        attachAura(player1, creature);

        advanceToEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Enchanted creature survives its controller's end step if it attacked")
    void sparesAttacker() {
        Permanent creature = addCreature(player1);
        creature.setAttackedThisTurn(true);
        attachAura(player1, creature);

        advanceToEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("The trigger uses the enchanted creature controller's end step")
    void triggersDuringEnchantedCreatureControllersEndStep() {
        Permanent creature = addCreature(player2);
        attachAura(player1, creature);

        advanceToEndStep(player1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        advanceToEndStep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Removing the Aura after the trigger does not stop the sacrifice")
    void triggerSurvivesAuraRemoval() {
        Permanent creature = addCreature(player1);
        Permanent aura = attachAura(player1, creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    private Permanent addCreature(Player controller) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(controller.getId()).add(creature);
        return creature;
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new InstillFuror());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
