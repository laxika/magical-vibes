package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TimbermawLarva;
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

@CardUsed({SoaringSeacliff.class, TimbermawLarva.class})
class SoaringSeacliffTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gives a target creature flying until end of turn")
    void entersTappedAndGrantsFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TimbermawLarva());
        harness.setHand(player1, List.of(new SoaringSeacliff()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent seacliff = findPermanent(player1, "Soaring Seacliff");
        assertThat(seacliff.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Can give flying to an opponent's creature")
    void canTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new TimbermawLarva());
        harness.setHand(player1, List.of(new SoaringSeacliff()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one blue mana")
    void tappingAddsBlueMana() {
        Permanent seacliff = harness.addToBattlefieldAndReturn(player1, new SoaringSeacliff());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(seacliff.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can enter tapped when there are no creatures to target")
    void entersWithoutCreatures() {
        harness.setHand(player1, List.of(new SoaringSeacliff()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Soaring Seacliff").isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flying is granted on resolution and only to the chosen creature")
    void grantsFlyingOnlyOnResolutionToChosenCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new TimbermawLarva());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new TimbermawLarva());
        harness.setHand(player1, List.of(new SoaringSeacliff()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gqs.hasKeyword(gd, chosen, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, chosen, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }
}
