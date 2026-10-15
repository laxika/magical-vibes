package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.w.WitheringTorment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValgavothsLair.class, WitheringTorment.class})
class ValgavothsLairTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and stores the chosen color")
    void entersTappedAndStoresChosenColor() {
        harness.setHand(player1, List.of(new ValgavothsLair()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        Permanent lair = findPermanent(player1, "Valgavoth's Lair");
        assertThat(lair.isTapped()).isTrue();
        assertThat(lair.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tappingAddsChosenColorMana() {
        Permanent lair = harness.addToBattlefieldAndReturn(player1, new ValgavothsLair());
        lair.setSummoningSick(false);
        lair.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(lair.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = CardColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Each color can be chosen on entry and produced without using the stack")
    void producesColorChosenOnEntry(CardColor color) {
        harness.setHand(player1, List.of(new ValgavothsLair()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, color.name());

        Permanent lair = findPermanent(player1, "Valgavoth's Lair");
        assertThat(lair.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor.name().equals(color.name()) ? 1 : 0);
        }
        assertThat(lair.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting the enchantment land")
    void opponentCannotTargetLair() {
        Permanent lair = harness.addToBattlefieldAndReturn(player2, new ValgavothsLair());
        harness.setHand(player1, List.of(new WitheringTorment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, lair.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player2, "Valgavoth's Lair");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Hexproof allows the controller to target the enchantment land")
    void controllerCanTargetLair() {
        Permanent lair = harness.addToBattlefieldAndReturn(player1, new ValgavothsLair());
        harness.setHand(player1, List.of(new WitheringTorment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, lair.getId());

        harness.assertNotOnBattlefield(player1, "Valgavoth's Lair");
        harness.assertInGraveyard(player1, "Valgavoth's Lair");
        harness.assertLife(player1, 18);
    }
}
