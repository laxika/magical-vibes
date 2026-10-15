package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.w.WaterfallAerialist;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScurridColony.class, Forest.class, WaterfallAerialist.class})
class ScurridColonyTest extends BaseCardTest {

    @Test
    @DisplayName("Remains 2/2 below eight lands")
    void noBonusBelowEightLands() {
        addLands(player1, 7);
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +2/+2 at eight lands")
    void getsBonusAtEightLands() {
        addLands(player1, 8);
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(4);
    }

    @Test
    @DisplayName("Loses the bonus when the controller drops below eight lands")
    void losesBonusWhenLandsDrop() {
        addLands(player1, 8);
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Forest"));

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's lands do not count")
    void opponentsLandsDoNotCount() {
        addLands(player2, 8);
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(2);
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    @Test
    @DisplayName("Gains the bonus immediately when the eighth land enters")
    void gainsBonusWhenEighthLandEnters() {
        addLands(player1, 7);
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(4);
    }

    @Test
    @DisplayName("More than eight lands grant only +2/+2 to each colony")
    void bonusDoesNotScaleOrStackAcrossColonies() {
        addLands(player1, 10);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ScurridColony());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Nonland permanents do not satisfy the land threshold")
    void nonlandsDoNotCount() {
        addLands(player1, 7);
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        harness.addToBattlefield(player1, new ScurridColony());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reach allows blocking a flying creature below the land threshold")
    void blocksFlyingCreatureWithoutLandBonus() {
        Permanent colony = addCreatureReady(player2, new ScurridColony());
        addCreatureReady(player1, new WaterfallAerialist());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(colony.isBlocking()).isTrue();
    }
}
