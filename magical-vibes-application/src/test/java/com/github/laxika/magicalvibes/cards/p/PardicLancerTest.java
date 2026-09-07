package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PardicLancer.class, Forest.class})
class PardicLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card at random gives +1/+0 and first strike")
    void discardsAndBoostsAndGrantsFirstStrike() {
        harness.addToBattlefield(player1, new PardicLancer());
        Permanent lancer = findPermanent(player1, "Pardic Lancer");
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(lancer.getPowerModifier()).isEqualTo(1);
        assertThat(lancer.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The boost and first strike wear off at end of turn")
    void boostAndFirstStrikeWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new PardicLancer());
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lancer = findPermanent(player1, "Pardic Lancer");
        assertThat(lancer.getPowerModifier()).isEqualTo(0);
        assertThat(lancer.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated with an empty hand")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new PardicLancer());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
