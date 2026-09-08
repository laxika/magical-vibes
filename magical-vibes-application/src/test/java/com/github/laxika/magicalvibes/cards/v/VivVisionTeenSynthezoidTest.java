package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(VivVisionTeenSynthezoid.class)
class VivVisionTeenSynthezoidTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when Viv Vision attacks with power 4 or greater")
    void drawsWhenAttackingWithHighPower() {
        Permanent viv = addCreatureReady(player1, new VivVisionTeenSynthezoid());
        viv.setPowerModifier(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when Viv Vision attacks with power less than 4")
    void doesNotDrawWhenAttackingWithLowPower() {
        addCreatureReady(player1, new VivVisionTeenSynthezoid());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));

        declareAttackers(List.of(0));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Entry-turn Power-up puts two +1/+1 counters on Viv Vision for four generic mana")
    void powerUpIsDiscountedDuringEntryTurn() {
        Permanent viv = harness.enterBattlefieldAndReturn(player1, new VivVisionTeenSynthezoid());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(viv.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power-up can be activated only once")
    void powerUpCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new VivVisionTeenSynthezoid());
        harness.addMana(player1, ManaColor.COLORLESS, 14);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
