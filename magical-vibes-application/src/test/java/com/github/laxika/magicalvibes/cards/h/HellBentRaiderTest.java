package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FloatingShield;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellBentRaider.class, FloatingShield.class})
class HellBentRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card at random grants protection from white until end of turn")
    void discardsAndGrantsProtectionFromWhite() {
        Permanent raider = addCreatureReady(player1, new HellBentRaider());
        harness.setHand(player1, List.of(new FloatingShield(), new FloatingShield()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, raider, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, raider, CardColor.RED)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Floating Shield");
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        Permanent raider = addCreatureReady(player1, new HellBentRaider());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasProtectionFrom(gd, raider, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Protection from white prevents a white spell from targeting the raider")
    void protectionStopsWhiteSpell() {
        Permanent raider = addCreatureReady(player1, new HellBentRaider());
        harness.setHand(player1, List.of(new FloatingShield()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new FloatingShield()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, raider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Protection from white wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent raider = addCreatureReady(player1, new HellBentRaider());
        harness.setHand(player1, List.of(new FloatingShield()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, raider, CardColor.WHITE)).isFalse();
    }
}
