package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ThranDynamo.class)
class ThranDynamoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Thran Dynamo produces three colorless mana")
    void tappingProducesThreeColorlessMana() {
        Permanent dynamo = harness.addToBattlefieldAndReturn(player1, new ThranDynamo());
        dynamo.setSummoningSick(false);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(dynamo.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A newly entered Thran Dynamo can be tapped immediately")
    void newlyEnteredDynamoCanBeTappedImmediately() {
        Permanent dynamo = harness.addToBattlefieldAndReturn(player1, new ThranDynamo());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(dynamo.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Thran Dynamo's mana ability resolves without using the stack")
    void manaAbilityDoesNotUseStack() {
        harness.addToBattlefield(player1, new ThranDynamo());

        harness.tapPermanent(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped Thran Dynamo cannot produce mana again")
    void cannotTapAgainWhileTapped() {
        harness.addToBattlefield(player1, new ThranDynamo());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }
}
