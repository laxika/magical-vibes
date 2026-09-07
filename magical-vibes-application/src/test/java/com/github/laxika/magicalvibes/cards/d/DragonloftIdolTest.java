package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonloftIdol.class, ShivanDragon.class})
class DragonloftIdolTest extends BaseCardTest {

    @Test
    @DisplayName("Is a 3/3 without a Dragon under its controller's control")
    void hasBaseStatsWithoutDragon() {
        Permanent idol = addCreatureReady(player1, new DragonloftIdol());

        assertThat(gqs.getEffectivePower(gd, idol)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, idol)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, idol, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, idol, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1, flying, and trample while its controller controls a Dragon")
    void getsBoostAndKeywordsWithDragon() {
        Permanent idol = addCreatureReady(player1, new DragonloftIdol());
        addCreatureReady(player1, new ShivanDragon());

        assertThat(gqs.getEffectivePower(gd, idol)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, idol)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, idol, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, idol, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Loses the boost and keywords when the Dragon leaves the battlefield")
    void losesBoostAndKeywordsWhenDragonLeaves() {
        Permanent idol = addCreatureReady(player1, new DragonloftIdol());
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());

        assertThat(gqs.getEffectivePower(gd, idol)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, idol, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(dragon);

        assertThat(gqs.getEffectivePower(gd, idol)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, idol)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, idol, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, idol, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Dragon does not enable the ability")
    void opponentDragonDoesNotCount() {
        Permanent idol = addCreatureReady(player1, new DragonloftIdol());
        addCreatureReady(player2, new ShivanDragon());

        assertThat(gqs.getEffectivePower(gd, idol)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, idol)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, idol, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, idol, Keyword.TRAMPLE)).isFalse();
    }
}
