package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PapercraftDecoy.class, WrathOfGod.class})
class PapercraftDecoyTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} after Papercraft Decoy leaves the battlefield draws a card")
    void payingDrawsCard() {
        harness.addToBattlefield(player1, new PapercraftDecoy());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setLibrary(player1, List.of(new PapercraftDecoy()));
        addWrathAndDecoyMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Papercraft Decoy");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining the payment does not draw a card")
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new PapercraftDecoy());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setLibrary(player1, List.of(new PapercraftDecoy()));
        addWrathAndDecoyMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Papercraft Decoy");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Accepting without enough mana does not draw a card")
    void acceptingWithoutEnoughManaDoesNotDraw() {
        harness.addToBattlefield(player1, new PapercraftDecoy());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setLibrary(player1, List.of(new PapercraftDecoy()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Papercraft Decoy");
    }

    private void addWrathAndDecoyMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
