package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SunkenPalace;
import com.github.laxika.magicalvibes.cards.t.ThornwoodFalls;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CopyLand.class, Island.class, SunkenPalace.class, ThornwoodFalls.class})
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

    @Test
    @DisplayName("Enters without copying when there are no lands")
    void entersWithoutCopyingWhenThereAreNoLands() {
        harness.castFromHand(player1, new CopyLand(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Copy Land");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Copying a tapped land does not copy its tapped state and grants its mana ability")
    void copiesOwnTappedLandAndCanProduceManaImmediately() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();
        harness.castFromHand(player1, new CopyLand(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, island.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(copy.isTapped()).isFalse();
        int blueBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
        harness.tapPermanent(player1, 1);
        assertThat(copy.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(blueBefore + 1);
        assertThat(island.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Copied land's enters-tapped replacement applies even when the original is untapped")
    void copiedLandEntersTapped() {
        Permanent palace = harness.addToBattlefieldAndReturn(player2, new SunkenPalace());
        palace.untap();
        harness.castFromHand(player1, new CopyLand(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, palace.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(copy.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(palace.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can copy another Copy Land that has already copied a land")
    void copiesAnExistingCopyLand() {
        Permanent palace = harness.addToBattlefieldAndReturn(player2, new SunkenPalace());
        harness.castFromHand(player1, new CopyLand(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, palace.getId());
        Permanent firstCopy = gd.playerBattlefields.get(player1.getId()).getFirst();
        firstCopy.untap();

        harness.castFromHand(player1, new CopyLand(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopy.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent secondCopy = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(secondCopy.isTapped()).isTrue();
        assertThat(secondCopy.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(secondCopy.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(firstCopy.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Copied land's entry trigger benefits Copy Land's controller")
    void copiedLandEntryTriggerUsesNewController() {
        Permanent falls = harness.addToBattlefieldAndReturn(player2, new ThornwoodFalls());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new CopyLand(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, falls.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        Permanent copy = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
    }
}
