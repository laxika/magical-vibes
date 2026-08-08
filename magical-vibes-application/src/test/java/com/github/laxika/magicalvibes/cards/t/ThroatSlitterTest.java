package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThroatSlitterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player destroys a chosen nonblack creature that player controls")
    void destroysChosenNonblackCreature() {
        Permanent slitter = addCreatureReady(player1, new ThroatSlitter());
        slitter.setAttacking(true);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Black creatures and the attacker's own creatures are not valid choices")
    void onlyDamagedPlayersNonblackCreatures() {
        Permanent slitter = addCreatureReady(player1, new ThroatSlitter());
        slitter.setAttacking(true);
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent enemyBlack = addCreatureReady(player2, new MassOfGhouls());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(enemyBears.getId())
                .doesNotContain(ownBears.getId())
                .doesNotContain(enemyBlack.getId());
    }

    @Test
    @DisplayName("No trigger when the damaged player controls only black creatures")
    void noTriggerWithoutNonblackCreatures() {
        Permanent slitter = addCreatureReady(player1, new ThroatSlitter());
        slitter.setAttacking(true);
        addCreatureReady(player2, new MassOfGhouls());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Mass of Ghouls");
    }
}
