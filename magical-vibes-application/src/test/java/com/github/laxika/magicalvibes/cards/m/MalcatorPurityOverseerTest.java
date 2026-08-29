package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MalcatorPurityOverseerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 3/3 Phyrexian Golem artifact creature token")
    void enteringCreatesGolemToken() {
        harness.setHand(player1, List.of(new MalcatorPurityOverseer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        assertThat(golem).isNotNull();
        assertThat(golem.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(golem.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(golem.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(golem.getEffectivePower()).isEqualTo(3);
        assertThat(golem.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("At your end step, creates a Golem after three artifacts entered under your control")
    void endStepCreatesGolemAfterThreeArtifactsEntered() {
        harness.addToBattlefield(player1, new MalcatorPurityOverseer());
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(), new ArrayList<>(List.of(
                new Ornithopter(), new Ornithopter(), new Ornithopter())));

        advanceToEndStep(player1);

        assertThat(countPermanents(player1, "Phyrexian Golem")).isEqualTo(1);
    }

    @Test
    @DisplayName("The end-step ability does not count opponent artifacts or fewer than three artifacts")
    void endStepRequiresThreeArtifactsUnderYourControl() {
        harness.addToBattlefield(player1, new MalcatorPurityOverseer());
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(), new ArrayList<>(List.of(
                new Ornithopter(), new Ornithopter())));
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), new ArrayList<>(List.of(new Ornithopter())));

        advanceToEndStep(player1);

        assertThat(countPermanents(player1, "Phyrexian Golem")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
