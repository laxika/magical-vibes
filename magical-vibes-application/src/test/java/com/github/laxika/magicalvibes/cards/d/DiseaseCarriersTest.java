package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DiseaseCarriersTest extends BaseCardTest {

    @Test
    @DisplayName("When Disease Carriers dies, target creature gets -2/-2")
    void deathTriggerGivesMinusTwoMinusTwo() {
        harness.addToBattlefield(player1, new DiseaseCarriers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        destroyDiseaseCarriers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The death trigger can target a creature controlled by either player")
    void deathTriggerCanTargetAnyCreature() {
        harness.addToBattlefield(player1, new DiseaseCarriers());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        destroyDiseaseCarriers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The death trigger -2/-2 kills a 2/2 creature")
    void deathTriggerKillsTwoTwoCreature() {
        harness.addToBattlefield(player1, new DiseaseCarriers());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        destroyDiseaseCarriers();

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DiseaseCarriers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        destroyDiseaseCarriers();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only creatures are legal death-trigger targets")
    void nonCreatureIsNotALegalTarget() {
        harness.addToBattlefield(player1, new DiseaseCarriers());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);

        destroyDiseaseCarriers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(artifact.getId());
    }

    @Test
    @DisplayName("The death trigger is skipped when no creature can be targeted")
    void deathTriggerSkippedWithNoCreatureTargets() {
        harness.addToBattlefield(player1, new DiseaseCarriers());

        destroyDiseaseCarriers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private void destroyDiseaseCarriers() {
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID diseaseCarriersId = harness.getPermanentId(player1, "Disease Carriers");
        harness.castInstant(player2, 0, diseaseCarriersId);
        harness.passBothPriorities();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
