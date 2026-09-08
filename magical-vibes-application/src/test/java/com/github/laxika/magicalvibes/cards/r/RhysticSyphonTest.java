package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RhysticSyphonTest extends BaseCardTest {

    @Test
    @DisplayName("Target player pays 3 mana to avoid the life loss and life gain")
    void targetPlayerPays() {
        harness.setHand(player1, List.of(new RhysticSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining the payment makes the target lose 5 life and the caster gain 5 life")
    void targetPlayerDeclines() {
        harness.setHand(player1, List.of(new RhysticSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("A target who cannot pay automatically loses 5 life and the caster gains 5 life")
    void targetPlayerCannotPay() {
        harness.setHand(player1, List.of(new RhysticSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
