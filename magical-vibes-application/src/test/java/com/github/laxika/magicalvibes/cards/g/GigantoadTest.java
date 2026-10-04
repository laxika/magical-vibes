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

    @Test
    @DisplayName("Boost updates immediately when the land count crosses seven in either direction")
    void boostTracksLandCountChanges() {
        Permanent gigantoad = addGigantoad(player1);
        addLands(player1, 6);

        assertThat(gqs.getEffectivePower(gd, gigantoad)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gigantoad)).isEqualTo(4);

        Permanent seventhLand = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, gigantoad)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, gigantoad)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(seventhLand);

        assertThat(gqs.getEffectivePower(gd, gigantoad)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gigantoad)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple Gigantoads each receive only their own boost with more than seven lands")
    void boostIsSelfOnlyAndDoesNotScaleWithLandCount() {
        Permanent first = addGigantoad(player1);
        Permanent second = addGigantoad(player1);
        Permanent opponent = addGigantoad(player2);
        addLands(player1, 8);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(4);
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
