package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkingSkyscraper.class, GrizzlyBears.class, Shock.class})
class WalkingSkyscraperTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each modified creature you control")
    void costsLessForEachModifiedCreatureYouControl() {
        Permanent modifiedCreature = addCreatureReady(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new WalkingSkyscraper()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Only modified creatures you control reduce the cost")
    void onlyModifiedCreaturesYouControlReduceTheCost() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new WalkingSkyscraper()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Has hexproof while untapped and loses it when tapped")
    void hasHexproofOnlyWhileUntapped() {
        Permanent skyscraper = addCreatureReady(player1, new WalkingSkyscraper());

        assertThat(gqs.hasKeyword(gd, skyscraper, Keyword.HEXPROOF)).isTrue();

        skyscraper.tap();

        assertThat(gqs.hasKeyword(gd, skyscraper, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Untapped Walking Skyscraper cannot be targeted")
    void untappedSkyscraperCannotBeTargeted() {
        Permanent skyscraper = addCreatureReady(player2, new WalkingSkyscraper());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, skyscraper.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }
}
