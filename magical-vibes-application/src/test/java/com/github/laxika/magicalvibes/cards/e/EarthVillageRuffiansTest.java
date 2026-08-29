package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthVillageRuffians.class, Forest.class, Murder.class})
class EarthVillageRuffiansTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, earthbends a land you control")
    void earthbendsLandWhenItDies() {
        Permanent ruffians = harness.addToBattlefieldAndReturn(player1, new EarthVillageRuffians());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        destroy(ruffians);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Its death trigger cannot target an opponent's land")
    void cannotTargetOpponentsLand() {
        Permanent ruffians = harness.addToBattlefieldAndReturn(player1, new EarthVillageRuffians());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        destroy(ruffians);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(ownLand.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).doesNotContain(opponentLand.getId());
    }

    private void destroy(Permanent permanent) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, permanent.getId());
        harness.passBothPriorities();
    }
}
