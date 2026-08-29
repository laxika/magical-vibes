package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DarkwatchElvesTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from black")
    void hasProtectionFromBlack() {
        Permanent elves = addCreatureReady(player1, new DarkwatchElves());

        assertThat(gqs.hasProtectionFrom(gd, elves, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, elves, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Cannot be targeted by a black spell")
    void cannotBeTargetedByBlackSpell() {
        Permanent elves = addCreatureReady(player1, new DarkwatchElves());
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DarkwatchElves()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Darkwatch Elves");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
