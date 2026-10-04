package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PatchworkGnomes;
import com.github.laxika.magicalvibes.cards.w.WildMongrel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaceOfFear.class, Forest.class, WildMongrel.class, PatchworkGnomes.class})
class FaceOfFearTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{B} and discarding a card grants fear until end of turn")
    void activatesAndGrantsFear() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfFear());
        Permanent otherFace = harness.addToBattlefieldAndReturn(player1, new FaceOfFear());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, face, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherFace, Keyword.FEAR)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Fear wears off at end of turn")
    void fearWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfFear());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, face, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new FaceOfFear());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped Face of Fear can activate on an opponent's turn and pays costs before resolution")
    void activatesWhileTappedOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfFear());
        face.tap();
        harness.setHand(player1, List.of(new FaceOfFear(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Face of Fear");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.hasKeyword(gd, face, Keyword.FEAR)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, face, Keyword.FEAR)).isTrue();
        assertThat(face.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted fear excludes green creatures but allows black and artifact creatures to block")
    void fearRestrictsBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfFear());
        Permanent green = harness.addToBattlefieldAndReturn(player2, new WildMongrel());
        Permanent black = harness.addToBattlefieldAndReturn(player2, new FaceOfFear());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PatchworkGnomes());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThat(bls.canBlockAttacker(gd, green, face, gd.playerBattlefields.get(player2.getId()))).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, green, face, gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, black, face, gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, artifact, face, gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Three colorless mana cannot pay the black component of the activation cost")
    void cannotActivateWithoutBlackMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfFear());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, face, Keyword.FEAR)).isFalse();
    }
}
