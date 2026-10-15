package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BitterbowSharpshooters;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelOfTheGodPharaoh.class, BitterbowSharpshooters.class})
class AngelOfTheGodPharaohTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new AngelOfTheGodPharaoh()));
        harness.setLibrary(player1, List.of(new BitterbowSharpshooters()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Angel of the God-Pharaoh");
        harness.assertInHand(player1, "Bitterbow Sharpshooters");
    }

    @Test
    void cyclingDiscardsImmediatelyAndDrawsOnlyOnResolution() {
        AngelOfTheGodPharaoh source = new AngelOfTheGodPharaoh();
        AngelOfTheGodPharaoh drawn = new AngelOfTheGodPharaoh();
        harness.setHand(player1, List.of(source));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclingCannotBePaidWithOnlyOneMana() {
        AngelOfTheGodPharaoh source = new AngelOfTheGodPharaoh();
        harness.setHand(player1, List.of(source));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        harness.assertNotInGraveyard(player1, "Angel of the God-Pharaoh");
        assertThat(gd.stack).isEmpty();
    }
}
