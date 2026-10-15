package com.github.laxika.magicalvibes.cards.v;

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

@CardUsed({VivVisionTeenSynthezoid.class})
class VivVisionTeenSynthezoidTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when Viv Vision attacks with power 4 or greater")
    void drawsWhenAttackingWithHighPower() {
        Permanent viv = addCreatureReady(player1, new VivVisionTeenSynthezoid());
        viv.setPowerModifier(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new VivVisionTeenSynthezoid()));

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
        harness.setLibrary(player1, List.of(new VivVisionTeenSynthezoid()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

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

    @Test
    @DisplayName("Cybernetic Senses triggers at low power and draws after Power-up resolves")
    void drawsWhenPowerUpRaisesPowerInResponse() {
        Permanent viv = addCreatureReady(player1, new VivVisionTeenSynthezoid());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new VivVisionTeenSynthezoid()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(viv.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw if power falls below four before resolution")
    void checksPowerAgainOnResolution() {
        Permanent viv = addCreatureReady(player1, new VivVisionTeenSynthezoid());
        viv.setPowerModifier(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new VivVisionTeenSynthezoid()));

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        viv.setPowerModifier(0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Uses Viv's power immediately before leaving the battlefield")
    void drawsUsingLastKnownPower() {
        Permanent viv = addCreatureReady(player1, new VivVisionTeenSynthezoid());
        viv.setPowerModifier(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new VivVisionTeenSynthezoid()));

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        viv.setToughnessModifier(-2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(viv);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw if Viv's last-known power was below four")
    void doesNotDrawUsingEarlierAttackPower() {
        Permanent viv = addCreatureReady(player1, new VivVisionTeenSynthezoid());
        viv.setPowerModifier(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new VivVisionTeenSynthezoid()));

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        viv.setPowerModifier(0);
        viv.setToughnessModifier(-2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(viv);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Power-up costs seven mana after the entry turn")
    void powerUpRequiresFullCostAfterEntryTurn() {
        Permanent viv = addCreatureReady(player1, new VivVisionTeenSynthezoid());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(viv.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(viv.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power-up cannot be activated a second time while the first activation is pending")
    void powerUpLimitAppliesBeforeResolution() {
        addCreatureReady(player1, new VivVisionTeenSynthezoid());
        harness.addMana(player1, ManaColor.COLORLESS, 14);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();
    }
}
