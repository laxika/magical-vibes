package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaritLagesSlumber.class, SnowCoveredIsland.class})
class MaritLagesSlumberTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield causes Marit Lage's Slumber to scry 1")
    void enteringCausesScry() {
        harness.castFromHand(player1, new MaritLagesSlumber(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("A snow permanent entering under your control causes a scry")
    void snowPermanentEnteringCausesScry() {
        harness.addToBattlefield(player1, new MaritLagesSlumber());
        harness.setHand(player1, List.of(new SnowCoveredIsland()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Ten snow permanents sacrifice Marit Lage's Slumber and create Marit Lage")
    void tenSnowPermanentsCreateMaritLage() {
        harness.addToBattlefield(player1, new MaritLagesSlumber());
        addSnowPermanents(9);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Marit Lage's Slumber");
        Permanent maritLage = findPermanent(player1, "Marit Lage");
        assertThat(maritLage.getEffectivePower()).isEqualTo(20);
        assertThat(maritLage.getEffectiveToughness()).isEqualTo(20);
        assertThat(maritLage.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(maritLage.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(maritLage.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Fewer than ten snow permanents do not sacrifice Marit Lage's Slumber")
    void fewerThanTenSnowPermanentsDoNotSacrifice() {
        harness.addToBattlefield(player1, new MaritLagesSlumber());
        addSnowPermanents(8);

        advanceToUpkeep(player1);

        harness.assertOnBattlefield(player1, "Marit Lage's Slumber");
        assertThat(findPermanents(player1, "Marit Lage")).isEmpty();
    }

    private void addSnowPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new SnowCoveredIsland());
        }
    }
}
