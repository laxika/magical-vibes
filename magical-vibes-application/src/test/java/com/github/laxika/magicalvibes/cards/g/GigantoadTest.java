package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gigantoad.class, Forest.class})
class GigantoadTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 with seven or more lands")
    void getsBoostWithSevenLands() {
        Permanent gigantoad = addGigantoad(player1);
        addLands(player1, 7);

        assertThat(gqs.getEffectivePower(gd, gigantoad)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, gigantoad)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not get the boost with fewer than seven lands")
    void doesNotGetBoostWithFewerThanSevenLands() {
        Permanent gigantoad = addGigantoad(player1);
        addLands(player1, 6);

        assertThat(gqs.getEffectivePower(gd, gigantoad)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gigantoad)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only lands controlled by Gigantoad's controller")
    void ignoresOpponentsLands() {
        Permanent gigantoad = addGigantoad(player1);
        addLands(player2, 7);

        assertThat(gqs.getEffectivePower(gd, gigantoad)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gigantoad)).isEqualTo(4);
    }

    private Permanent addGigantoad(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Gigantoad());
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
