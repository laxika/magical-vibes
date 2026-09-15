package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatterAssumptions.class, BorosSwiftblade.class, DarksteelRelic.class,
        Forest.class, GrizzlyBears.class})
class ShatterAssumptionsTest extends BaseCardTest {

    @Test
    @DisplayName("Colorless nonland mode discards only colorless nonland cards")
    void colorlessNonlandMode() {
        DarksteelRelic colorlessNonland = new DarksteelRelic();
        Forest land = new Forest();
        GrizzlyBears monocolored = new GrizzlyBears();
        BorosSwiftblade multicolored = new BorosSwiftblade();
        harness.setHand(player2, new ArrayList<>(List.of(
                colorlessNonland, land, monocolored, multicolored)));
        harness.setHand(player1, List.of(new ShatterAssumptions()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactly(land, monocolored, multicolored);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(colorlessNonland);
    }

    @Test
    @DisplayName("Multicolored mode discards all multicolored cards")
    void multicoloredMode() {
        DarksteelRelic colorless = new DarksteelRelic();
        Forest land = new Forest();
        GrizzlyBears monocolored = new GrizzlyBears();
        BorosSwiftblade multicolored = new BorosSwiftblade();
        harness.setHand(player2, new ArrayList<>(List.of(
                colorless, land, monocolored, multicolored)));
        harness.setHand(player1, List.of(new ShatterAssumptions()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactly(colorless, land, monocolored);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(multicolored);
    }

    @Test
    @DisplayName("Both modes can target only an opponent")
    void bothModesRejectTheCasterAsTarget() {
        harness.setHand(player1, List.of(new ShatterAssumptions()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
