package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GeneratedHorizons.class)
class GeneratedHorizonsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an untapped Forest land token during its controller's upkeep")
    void createsForestLandTokenDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new GeneratedHorizons());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.getCard().isToken()).isTrue();
        assertThat(forest.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(forest.getCard().getSubtypes()).containsExactly(CardSubtype.FOREST);
        assertThat(forest.isTapped()).isFalse();

        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forest);
        harness.activateAbility(player1, forestIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not create a Forest during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new GeneratedHorizons());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Forest")).isEmpty();
    }
}
