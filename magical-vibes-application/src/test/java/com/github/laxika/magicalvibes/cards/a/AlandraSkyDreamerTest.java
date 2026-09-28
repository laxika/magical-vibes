package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlandraSkyDreamer.class, Forest.class})
class AlandraSkyDreamerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying Drake on the second draw and boosts Alandra and Drakes on the fifth")
    void createsAndBoostsOnDrawThresholds() {
        Permanent alandra = harness.addToBattlefieldAndReturn(player1, new AlandraSkyDreamer());
        harness.setHand(player1, List.of());
        setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        draw(player1);
        draw(player1);
        assertThat(findPermanents(player1, "Drake")).isEmpty();

        harness.passBothPriorities();

        List<Permanent> drakes = findPermanents(player1, "Drake");
        assertThat(drakes).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, drakes.get(0))).isEqualTo(2);

        draw(player1);
        draw(player1);
        draw(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, alandra)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, drakes.get(0))).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, drakes.get(0))).isEqualTo(7);

        draw(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, alandra)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, drakes.get(0))).isEqualTo(7);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void setLibrary(Player player, List<Card> cards) {
        UUID playerId = player.getId();
        gd.playerDecks.get(playerId).clear();
        gd.playerDecks.get(playerId).addAll(cards);
    }
}
