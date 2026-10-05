package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.cards.t.Terrarion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongRoadHome.class, FieldCreeper.class, Terrarion.class})
class LongRoadHomeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature and returns it at the next end step with a +1/+1 counter")
    void returnsCreatureWithCounterAtNextEndStep() {
        harness.addToBattlefield(player1, new FieldCreeper());
        harness.setHand(player1, List.of(new LongRoadHome()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID originalId = harness.getPermanentId(player1, "Field Creeper");
        harness.castAndResolveInstant(player1, 0, originalId);

        harness.assertNotOnBattlefield(player1, "Field Creeper");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Field Creeper"));

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Field Creeper");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Field Creeper"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Terrarion());
        harness.setHand(player1, List.of(new LongRoadHome()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID artifactId = harness.getPermanentId(player1, "Terrarion");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns an opponent's creature on the caster's end step")
    void returnsOpponentsCreatureAtNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new FieldCreeper());
        harness.setHand(player1, List.of(new LongRoadHome()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Field Creeper"));

        harness.assertNotOnBattlefield(player2, "Field Creeper");
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Field Creeper");
        assertThat(findPermanent(player2, "Field Creeper").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Returns a stolen creature to its owner instead of its previous controller")
    void returnsStolenCreatureToOwner() {
        FieldCreeper card = new FieldCreeper();
        card.setOwnerId(player2.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        harness.setHand(player1, List.of(new LongRoadHome()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, stolen.getId());
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Field Creeper");
        assertThat(findPermanent(player2, "Field Creeper").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Return waits for the delayed triggered ability to resolve")
    void returnUsesTheStack() {
        harness.addToBattlefield(player1, new FieldCreeper());
        harness.setHand(player1, List.of(new LongRoadHome()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Field Creeper"));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Field Creeper");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Field Creeper").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("A spell resolved during the end step waits until the following turn's end step")
    void castDuringEndStepWaitsUntilFollowingEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new FieldCreeper());
        harness.setHand(player1, List.of(new LongRoadHome()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Field Creeper"));

        harness.assertNotOnBattlefield(player1, "Field Creeper");
        assertThat(gd.stack).isEmpty();
        harness.passUntilWithNoAttackers(player2, TurnStep.POSTCOMBAT_MAIN);
        harness.assertNotOnBattlefield(player1, "Field Creeper");
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Field Creeper").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Return creates a fresh untapped permanent with only the new counter")
    void returnDoesNotPreserveOldCountersOrTappedStatus() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new FieldCreeper());
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        original.tap();
        harness.setHand(player1, List.of(new LongRoadHome()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Field Creeper");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
