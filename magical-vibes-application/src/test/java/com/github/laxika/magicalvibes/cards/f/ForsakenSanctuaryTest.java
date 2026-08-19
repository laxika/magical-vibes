package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForsakenSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ForsakenSanctuary()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Forsaken Sanctuary").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one white mana")
    void tapAddsWhiteMana() {
        harness.addToBattlefield(player1, new ForsakenSanctuary());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability adds one black mana")
    void tapAddsBlackMana() {
        harness.addToBattlefield(player1, new ForsakenSanctuary());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
