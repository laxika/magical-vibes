package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AutumnalGloomTest extends BaseCardTest {

    @Test
    void activatedAbilityMillsOneCard() {
        harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());
        Card milledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milledCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledCard);
    }

    @Test
    void transformsAtEndStepWithDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gloom.isTransformed()).isTrue();
    }

    @Test
    void doesNotTransformAtEndStepWithoutDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());

        advanceToEndStep();

        assertThat(gloom.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
