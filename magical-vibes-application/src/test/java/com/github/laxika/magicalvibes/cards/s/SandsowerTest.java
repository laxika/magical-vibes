package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sandsower.class, GrizzlyBears.class})
class SandsowerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping three creatures taps target creature")
    void tappingThreeCreaturesTapsTarget() {
        Permanent sandsower = addCreatureReady(player1, new Sandsower());
        Permanent creatureA = addCreatureReady(player1, new GrizzlyBears());
        Permanent creatureB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(sandsower.isTapped()).isTrue();
        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing three of four creatures leaves the fourth untapped")
    void choosesThreeOfFourCreatures() {
        Permanent sandsower = addCreatureReady(player1, new Sandsower());
        Permanent creatureA = addCreatureReady(player1, new GrizzlyBears());
        Permanent creatureB = addCreatureReady(player1, new GrizzlyBears());
        Permanent spare = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        for (Permanent creature : List.of(sandsower, creatureA, creatureB)) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.passBothPriorities();

        assertThat(sandsower.isTapped()).isTrue();
        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        assertThat(spare.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without three untapped creatures")
    void cannotActivateWithoutThreeUntappedCreatures() {
        addCreatureReady(player1, new Sandsower());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNonCreatureTarget() {
        addCreatureReady(player1, new Sandsower());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
