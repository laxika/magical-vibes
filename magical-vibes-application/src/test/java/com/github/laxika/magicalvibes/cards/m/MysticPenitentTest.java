package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticPenitent.class, AvenFlock.class})
class MysticPenitentTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 without threshold")
    void baseStatsWithoutThreshold() {
        harness.addToBattlefield(player1, new MysticPenitent());

        assertStats(1, 1, false);
    }

    @Test
    @DisplayName("Gets +1/+1 and flying with seven cards in controller's graveyard")
    void thresholdAtSevenCards() {
        fillGraveyard(player1, 7);
        harness.addToBattlefield(player1, new MysticPenitent());

        assertStats(2, 2, true);
    }

    @Test
    @DisplayName("Six cards are not enough for threshold")
    void noThresholdAtSixCards() {
        fillGraveyard(player1, 6);
        harness.addToBattlefield(player1, new MysticPenitent());

        assertStats(1, 1, false);
    }

    @Test
    @DisplayName("Opponent's graveyard does not count")
    void opponentGraveyardDoesNotCount() {
        fillGraveyard(player2, 7);
        harness.addToBattlefield(player1, new MysticPenitent());

        assertStats(1, 1, false);
    }

    @Test
    @DisplayName("Loses the bonus when graveyard drops below seven cards")
    void losesBonusWhenGraveyardShrinks() {
        fillGraveyard(player1, 7);
        harness.addToBattlefield(player1, new MysticPenitent());
        Permanent penitent = findPenitent();
        assertStats(2, 2, true);

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, penitent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, penitent)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, penitent, Keyword.FLYING)).isFalse();
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new AvenFlock());
        }
        harness.setGraveyard(player, cards);
    }

    @Test
    @DisplayName("Gains the bonus immediately when the seventh card enters its controller's graveyard")
    void gainsBonusWhenGraveyardGrows() {
        fillGraveyard(player1, 6);
        harness.addToBattlefield(player1, new MysticPenitent());
        assertStats(1, 1, false);

        gd.playerGraveyards.get(player1.getId()).add(new AvenFlock());

        assertStats(2, 2, true);
    }

    @Test
    @DisplayName("Threshold does not boost other creatures")
    void thresholdBonusAppliesOnlyToSelf() {
        fillGraveyard(player1, 7);
        harness.addToBattlefield(player1, new MysticPenitent());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new AvenFlock());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new MysticPenitent());

        assertStats(2, 2, true);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Attacking without threshold does not tap Mystic Penitent")
    void vigilanceWithoutThreshold() {
        Permanent penitent = addCreatureReady(player1, new MysticPenitent());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(penitent.isAttacking()).isTrue();
        assertThat(penitent.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attacking with threshold does not tap Mystic Penitent")
    void vigilanceWithThreshold() {
        fillGraveyard(player1, 7);
        Permanent penitent = addCreatureReady(player1, new MysticPenitent());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(penitent.isAttacking()).isTrue();
        assertThat(penitent.isTapped()).isFalse();
    }

    private Permanent findPenitent() {
        return findPermanent(player1, "Mystic Penitent");
    }

    private void assertStats(int power, int toughness, boolean flying) {
        Permanent penitent = findPenitent();
        assertThat(gqs.getEffectivePower(gd, penitent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, penitent)).isEqualTo(toughness);
        assertThat(gqs.hasKeyword(gd, penitent, Keyword.FLYING)).isEqualTo(flying);
    }
}
