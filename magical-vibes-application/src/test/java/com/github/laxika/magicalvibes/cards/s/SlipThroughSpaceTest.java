package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlipThroughSpace.class, GrizzlyBears.class, Forest.class})
class SlipThroughSpaceTest extends BaseCardTest {

    @Test
    @DisplayName("Makes a target creature unblockable and draws a card")
    void makesCreatureUnblockableAndDrawsCard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SlipThroughSpace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(GrizzlyBears.class);
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SlipThroughSpace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SlipThroughSpace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
