package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({PardicLancer.class, PardicArsonist.class})
class PardicLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card at random gives +1/+0 and first strike")
    void discardsAndBoostsAndGrantsFirstStrike() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new PardicLancer());
        harness.setHand(player1, List.of(new PardicArsonist()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(lancer.getPowerModifier()).isEqualTo(1);
        assertThat(lancer.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Pardic Arsonist");
    }

    @Test
    @DisplayName("The random discard is paid before the ability resolves")
    void paysRandomDiscardAsActivationCost() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new PardicLancer());
        harness.setHand(player1, List.of(new PardicArsonist()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Pardic Arsonist");
        assertThat(lancer.getPowerModifier()).isEqualTo(0);
        assertThat(lancer.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(lancer.getPowerModifier()).isEqualTo(1);
        assertThat(lancer.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The boost and first strike wear off at end of turn")
    void boostAndFirstStrikeWearOffAtEndOfTurn() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new PardicLancer());
        harness.setHand(player1, List.of(new PardicArsonist()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lancer.getPowerModifier()).isEqualTo(0);
        assertThat(lancer.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations discard one card each and their power boosts stack")
    void repeatedActivationsStackWithoutTappingTheSource() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new PardicLancer());
        PardicArsonist arsonist = new PardicArsonist();
        PardicLancer handLancer = new PardicLancer();
        harness.setHand(player1, List.of(arsonist, handLancer));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst())
                .isIn(arsonist, handLancer);
        assertThat(gd.playerHands.get(player1.getId()).getFirst())
                .isNotSameAs(gd.playerGraveyards.get(player1.getId()).getFirst());
        assertThat(lancer.getPowerModifier()).isZero();

        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(arsonist, handLancer);
        assertThat(lancer.getPowerModifier()).isEqualTo(2);
        assertThat(lancer.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(lancer.isTapped()).isFalse();
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
