package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YellowjacketHeartlessMarauder.class, DocOcksHenchmen.class, GrizzlyBears.class})
class YellowjacketHeartlessMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 and lifelink when another Villain enters under your control")
    void triggersForAnotherVillainYouControl() {
        Permanent yellowjacket = harness.addToBattlefieldAndReturn(player1, new YellowjacketHeartlessMarauder());
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(yellowjacket.getPowerModifier()).isEqualTo(1);
        assertThat(yellowjacket.getToughnessModifier()).isZero();
        assertThat(yellowjacket.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger for a non-Villain creature")
    void doesNotTriggerForNonVillain() {
        Permanent yellowjacket = harness.addToBattlefieldAndReturn(player1, new YellowjacketHeartlessMarauder());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(yellowjacket.getPowerModifier()).isZero();
        assertThat(yellowjacket.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's Villain")
    void doesNotTriggerForOpponentsVillain() {
        Permanent yellowjacket = harness.addToBattlefieldAndReturn(player1, new YellowjacketHeartlessMarauder());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DocOcksHenchmen()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(yellowjacket.getPowerModifier()).isZero();
        assertThat(yellowjacket.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost and lifelink wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent yellowjacket = harness.addToBattlefieldAndReturn(player1, new YellowjacketHeartlessMarauder());
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(yellowjacket.getPowerModifier()).isZero();
        assertThat(yellowjacket.hasKeyword(Keyword.LIFELINK)).isFalse();
    }
}
