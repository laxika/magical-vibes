package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkolaGrovedancer.class, Forest.class, GrizzlyBears.class})
class SkolaGrovedancerTest extends BaseCardTest {

    @Test
    @DisplayName("Milling a land gains 1 life")
    void millingLandGainsLife() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveStack();

        harness.assertLife(player1, 21);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Milling a nonland card does not gain life")
    void millingNonlandDoesNotGainLife() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        harness.setLife(player1, 20);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveStack();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void resolveStack() {
        int guard = 0;
        while ((!gd.stack.isEmpty() || gd.interaction.activeInteraction() != null) && guard++ < 20) {
            harness.passBothPriorities();
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
