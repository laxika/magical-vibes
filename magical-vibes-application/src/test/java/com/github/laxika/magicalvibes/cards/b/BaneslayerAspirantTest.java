package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BaneslayerAspirant.class)
class BaneslayerAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("Without an emblem, Baneslayer Aspirant has no bonus")
    void noEmblemMeansNoBonus() {
        Permanent aspirant = addCreatureReady(player1, new BaneslayerAspirant());

        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Having an emblem gives Baneslayer Aspirant +3/+3 and three keywords")
    void emblemGrantsBonus() {
        Permanent aspirant = addCreatureReady(player1, new BaneslayerAspirant());
        gd.emblems.add(new Emblem(player1.getId(), List.of(), null));

        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("An opponent's emblem does not satisfy Baneslayer Aspirant")
    void opponentEmblemDoesNotGrantBonus() {
        Permanent aspirant = addCreatureReady(player1, new BaneslayerAspirant());
        gd.emblems.add(new Emblem(player2.getId(), List.of(), null));

        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Multiple emblems grant the bonus only once")
    void multipleEmblemsDoNotMultiplyBonus() {
        Permanent aspirant = addCreatureReady(player1, new BaneslayerAspirant());
        gd.emblems.add(new Emblem(player1.getId(), List.of(), null));
        gd.emblems.add(new Emblem(player1.getId(), List.of(), null));

        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Each Aspirant grants its bonus only to itself")
    void multipleAspirantsDoNotBoostEachOther() {
        Permanent first = addCreatureReady(player1, new BaneslayerAspirant());
        Permanent second = addCreatureReady(player1, new BaneslayerAspirant());
        gd.emblems.add(new Emblem(player1.getId(), List.of(), null));

        for (Permanent aspirant : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("The bonus follows the current controller's emblems")
    void changingControllerReevaluatesBonus() {
        Permanent aspirant = addCreatureReady(player1, new BaneslayerAspirant());
        gd.emblems.add(new Emblem(player1.getId(), List.of(), null));
        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(aspirant);
        gd.playerBattlefields.get(player2.getId()).add(aspirant);

        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.LIFELINK)).isFalse();

        gd.emblems.add(new Emblem(player2.getId(), List.of(), null));

        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.LIFELINK)).isTrue();
    }
}
