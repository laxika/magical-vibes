package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sandsower.class, BorosRecruit.class, Plains.class})
class SandsowerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping three creatures taps target creature")
    void tappingThreeCreaturesTapsTarget() {
        Permanent sandsower = addCreatureReady(player1, new Sandsower());
        Permanent creatureA = addCreatureReady(player1, new BorosRecruit());
        Permanent creatureB = addCreatureReady(player1, new BorosRecruit());
        Permanent target = addCreatureReady(player2, new BorosRecruit());

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
        Permanent creatureA = addCreatureReady(player1, new BorosRecruit());
        Permanent creatureB = addCreatureReady(player1, new BorosRecruit());
        Permanent spare = addCreatureReady(player1, new BorosRecruit());
        Permanent target = addCreatureReady(player2, new BorosRecruit());

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
        addCreatureReady(player1, new BorosRecruit());
        Permanent tappedCreature = addCreatureReady(player1, new BorosRecruit());
        tappedCreature.tap();
        Permanent target = addCreatureReady(player2, new BorosRecruit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot count a noncreature toward the activation cost")
    void cannotCountNonCreatureTowardActivationCost() {
        addCreatureReady(player1, new Sandsower());
        addCreatureReady(player1, new BorosRecruit());
        harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent target = addCreatureReady(player2, new BorosRecruit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNonCreatureTarget() {
        Permanent sandsower = addCreatureReady(player1, new Sandsower());
        Permanent creatureA = addCreatureReady(player1, new BorosRecruit());
        Permanent creatureB = addCreatureReady(player1, new BorosRecruit());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sandsower.isTapped()).isFalse();
        assertThat(creatureA.isTapped()).isFalse();
        assertThat(creatureB.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick creatures including Sandsower can pay the cost")
    void summoningSickCreaturesCanPayCost() {
        Permanent sandsower = harness.addToBattlefieldAndReturn(player1, new Sandsower());
        Permanent creatureA = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent creatureB = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent target = addCreatureReady(player2, new BorosRecruit());
        sandsower.setSummoningSick(true);
        creatureA.setSummoningSick(true);
        creatureB.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(sandsower.isTapped()).isTrue();
        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped Sandsower can activate by tapping three other creatures")
    void tappedSandsowerCanActivate() {
        Permanent sandsower = addCreatureReady(player1, new Sandsower());
        sandsower.tap();
        Permanent creatureA = addCreatureReady(player1, new BorosRecruit());
        Permanent creatureB = addCreatureReady(player1, new BorosRecruit());
        Permanent creatureC = addCreatureReady(player1, new BorosRecruit());
        Permanent target = addCreatureReady(player2, new BorosRecruit());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        assertThat(creatureC.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature tapped to pay the cost can also be the target")
    void costPayerCanAlsoBeTarget() {
        Permanent sandsower = addCreatureReady(player1, new Sandsower());
        Permanent creatureA = addCreatureReady(player1, new BorosRecruit());
        Permanent creatureB = addCreatureReady(player1, new BorosRecruit());

        harness.activateAbility(player1, 0, null, sandsower.getId());
        harness.passBothPriorities();

        assertThat(sandsower.isTapped()).isTrue();
        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

}
