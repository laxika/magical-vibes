package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ConstructACosmicCube;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoctorDoom.class, ConstructACosmicCube.class, GrizzlyBears.class})
class DoctorDoomTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 3/3 Doombot artifact creature tokens when it enters")
    void createsDoombotsWhenEntering() {
        harness.enterBattlefieldAndReturn(player1, new DoctorDoom());
        resolveAllTriggers();

        List<Permanent> doombots = findPermanents(player1, "Doombot");

        assertThat(doombots).hasSize(2);
        assertThat(doombots).allSatisfy(doombot -> {
            assertThat(doombot.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(doombot.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(doombot.getCard().getSubtypes())
                    .contains(CardSubtype.ROBOT, CardSubtype.VILLAIN);
            assertThat(doombot.getEffectivePower()).isEqualTo(3);
            assertThat(doombot.getEffectiveToughness()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("Has indestructible while its controller controls an artifact creature")
    void hasIndestructibleWithArtifactCreature() {
        Permanent doctor = harness.enterBattlefieldAndReturn(player1, new DoctorDoom());
        resolveAllTriggers();
        doctor.setMarkedDamage(3);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(doctor);

        findPermanents(player1, "Doombot").forEach(doombot -> doombot.setMarkedDamage(3));
        harness.runStateBasedActions();
        assertThat(findPermanents(player1, "Doombot")).isEmpty();

        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doctor);
    }

    @Test
    @DisplayName("Has indestructible while its controller controls a Plan")
    void hasIndestructibleWithPlan() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new DoctorDoom());
        harness.addToBattlefield(player1, new ConstructACosmicCube());
        doctor.setMarkedDamage(3);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(doctor);
    }

    @Test
    @DisplayName("Draws a card and loses one life at its controller's end step")
    void drawsAndLosesLifeAtEndStep() {
        harness.addToBattlefieldAndReturn(player1, new DoctorDoom());
        harness.setHand(player1, List.of());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
