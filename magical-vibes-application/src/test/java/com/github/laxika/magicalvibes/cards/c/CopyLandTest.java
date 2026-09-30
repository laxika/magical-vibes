package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CopyLand.class, Island.class})
class CopyLandTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a land and remains an enchantment")
    void copiesLandAndRemainsAnEnchantment() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.castFromHand(player1, new CopyLand(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, island.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(copy.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(copy.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
    }

    @Test
    @DisplayName("Enters as Copy Land when the copy choice is declined")
    void entersAsCopyLandWhenCopyIsDeclined() {
        harness.addToBattlefield(player2, new Island());
        harness.castFromHand(player1, new CopyLand(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        Permanent copyLand = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(copyLand.getCard().hasType(CardType.LAND)).isFalse();
        assertThat(copyLand.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
    }
}
