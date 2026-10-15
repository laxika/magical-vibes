package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ConstructACosmicCube;
import com.github.laxika.magicalvibes.cards.f.FalconsWingHarness;
import com.github.laxika.magicalvibes.cards.h.HydraTroopers;
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

@CardUsed({DoctorDoom.class, ConstructACosmicCube.class, FalconsWingHarness.class, HydraTroopers.class})
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
        HydraTroopers troopers = new HydraTroopers();
        harness.setLibrary(player1, List.of(troopers));

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(troopers);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An ordinary creature does not grant indestructible")
    void ordinaryCreatureDoesNotGrantIndestructible() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new DoctorDoom());
        harness.addToBattlefield(player1, new HydraTroopers());
        doctor.setMarkedDamage(3);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doctor);
        harness.assertInGraveyard(player1, "Doctor Doom");
    }

    @Test
    @DisplayName("An opponent's artifact creatures do not grant indestructible")
    void opponentsArtifactCreaturesDoNotGrantIndestructible() {
        harness.enterBattlefieldAndReturn(player2, new DoctorDoom());
        resolveAllTriggers();
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new DoctorDoom());
        doctor.setMarkedDamage(3);

        harness.runStateBasedActions();

        assertThat(findPermanents(player2, "Doombot")).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doctor);
    }

    @Test
    @DisplayName("A noncreature artifact and a separate creature do not grant indestructible")
    void artifactAndCreatureMustBeTheSamePermanent() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new DoctorDoom());
        harness.addToBattlefield(player1, new FalconsWingHarness());
        harness.addToBattlefield(player1, new HydraTroopers());
        doctor.setMarkedDamage(3);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doctor);
    }

    @Test
    @DisplayName("An opponent's Plan does not grant indestructible")
    void opponentsPlanDoesNotGrantIndestructible() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new DoctorDoom());
        harness.addToBattlefield(player2, new ConstructACosmicCube());
        doctor.setMarkedDamage(3);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doctor);
    }

    @Test
    @DisplayName("Loses indestructible immediately when the last Plan leaves")
    void losesIndestructibleWhenPlanLeaves() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new DoctorDoom());
        Permanent plan = harness.addToBattlefieldAndReturn(player1, new ConstructACosmicCube());
        doctor.setMarkedDamage(3);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(doctor);

        gd.playerBattlefields.get(player1.getId()).remove(plan);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doctor);
    }

    @Test
    @DisplayName("Does not draw or lose life at an opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new DoctorDoom());
        harness.setHand(player1, List.of());
        DoctorDoom topCard = new DoctorDoom();
        harness.setLibrary(player1, List.of(topCard));

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The end-step trigger resolves after Doctor Doom leaves the battlefield")
    void endStepTriggerResolvesWithoutSource() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new DoctorDoom());
        harness.setHand(player1, List.of());
        DoctorDoom topCard = new DoctorDoom();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        doctor.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Doctor Doom");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("The enter trigger creates Doombots even if Doctor Doom dies before it resolves")
    void enterTriggerResolvesWithoutSource() {
        Permanent doctor = harness.enterBattlefieldAndReturn(player1, new DoctorDoom());
        doctor.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Doctor Doom");

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Doombot")).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doctor);
    }

    @Test
    @DisplayName("Doombots enter untapped and colorless")
    void doombotsAreUntappedAndColorless() {
        harness.enterBattlefieldAndReturn(player1, new DoctorDoom());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Doombot")).hasSize(2).allSatisfy(doombot -> {
            assertThat(doombot.isTapped()).isFalse();
            assertThat(doombot.getCard().getColors()).isEmpty();
            assertThat(doombot.getCard().getColor()).isNull();
        });
    }

    @Test
    @DisplayName("The end-step ability draws and loses life for its controller only")
    void endStepAbilityAffectsItsControllerOnly() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.UPKEEP));
        harness.addToBattlefield(player2, new DoctorDoom());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        DoctorDoom topCard = new DoctorDoom();
        harness.setLibrary(player2, List.of(topCard));

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
