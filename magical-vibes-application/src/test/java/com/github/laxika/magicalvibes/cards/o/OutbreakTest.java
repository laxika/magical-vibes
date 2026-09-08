package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutbreakTest extends BaseCardTest {

    private void castOutbreak(Player caster) {
        harness.setHand(caster, List.of(new Outbreak()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 3);
        harness.castSorcery(caster, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("All creatures of the chosen type get -1/-1")
    void weakensAllCreaturesOfChosenType() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        castOutbreak(player1);
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanent(player1, "Grizzly Bears").getPowerModifier()).isEqualTo(-1);
        assertThat(findPermanent(player2, "Grizzly Bears").getPowerModifier()).isEqualTo(-1);
        assertThat(findPermanent(player1, "Hill Giant").getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The -1/-1 modifier wears off at end of turn")
    void modifierWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castOutbreak(player1);
        harness.handleListChoice(player1, "BEAR");
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can be cast by discarding a Swamp")
    void canBeCastByDiscardingSwamp() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Outbreak(), new Swamp()));

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        harness.assertInGraveyard(player1, "Outbreak");
        harness.assertInGraveyard(player1, "Swamp");
        assertThat(findPermanent(player2, "Grizzly Bears").getPowerModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("The alternate cost requires a Swamp")
    void alternateCostRequiresSwamp() {
        harness.setHand(player1, List.of(new Outbreak(), new Mountain()));

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
    }
}
