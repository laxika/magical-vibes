package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LingeringDeath.class, GrizzlyBears.class})
class LingeringDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices the enchanted creature at the beginning of its controller's end step")
    void sacrificesAtEnchantedControllerEndStep() {
        Permanent creature = attachToOpponentCreature();

        runEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getId().equals(creature.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger during the Aura controller's end step")
    void doesNotTriggerDuringAuraControllerEndStep() {
        Permanent creature = attachToOpponentCreature();

        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent attachToOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LingeringDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    private void runEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
