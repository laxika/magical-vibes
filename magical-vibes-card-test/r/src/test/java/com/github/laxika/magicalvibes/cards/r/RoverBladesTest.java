package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoverBlades.class, GrizzlyBears.class})
class RoverBladesTest extends BaseCardTest {

    @Test
    void equippedCreatureHasDoubleStrike() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent roverBlades = addCreatureReady(player1, new RoverBlades());
        roverBlades.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void equipAttachesToTargetCreature() {
        Permanent roverBlades = addCreatureReady(player1, new RoverBlades());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(roverBlades.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void crewAnimatesVehicleWithDoubleStrikeAndTapsCrewUntilEndOfTurn() {
        Permanent roverBlades = addCreatureReady(player1, new RoverBlades());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, roverBlades)).isTrue();
        assertThat(gqs.hasKeyword(gd, roverBlades, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(crew.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, roverBlades)).isFalse();
    }

    @Test
    void crewingEquippedVehicleDetachesItAndRemovesGrantedDoubleStrike() {
        Permanent roverBlades = addCreatureReady(player1, new RoverBlades());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, roverBlades)).isTrue();
        assertThat(roverBlades.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, roverBlades, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, roverBlades)).isFalse();
        assertThat(roverBlades.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void crewCanTapSummoningSickCreatureWithoutTappingVehicle() {
        Permanent roverBlades = harness.addToBattlefieldAndReturn(player1, new RoverBlades());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(roverBlades.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, roverBlades)).isTrue();
    }

    @Test
    void crewInResponseToEquipPreventsAttachment() {
        Permanent roverBlades = addCreatureReady(player1, new RoverBlades());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, roverBlades)).isTrue();
        assertThat(roverBlades.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

}
