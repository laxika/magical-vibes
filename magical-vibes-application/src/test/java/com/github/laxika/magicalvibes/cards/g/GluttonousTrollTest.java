package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GluttonousTroll.class, GrizzlyBears.class, Forest.class})
@DisplayName("Gluttonous Troll")
class GluttonousTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Food token for its opponent when it enters")
    void createsFoodTokenForEachOpponent() {
        castTroll();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifices another nonland permanent to get +2/+2 until end of turn")
    void sacrificesAnotherNonlandPermanentAndBoosts() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new GluttonousTroll());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getEffectivePower()).isEqualTo(5);
        assertThat(troll.getEffectiveToughness()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice a land or the Troll itself")
    void cannotSacrificeLandOrSource() {
        harness.addToBattlefield(player1, new GluttonousTroll());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTroll() {
        harness.setHand(player1, List.of(new GluttonousTroll()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
