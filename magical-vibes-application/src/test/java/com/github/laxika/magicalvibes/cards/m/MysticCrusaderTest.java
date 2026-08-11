package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MysticCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats and no flying before threshold")
    void baseStatsBeforeThreshold() {
        harness.addToBattlefield(player1, new MysticCrusader());

        assertStats(2, 1);
        assertThat(gqs.hasKeyword(gd, findCrusader(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and flying with seven cards in its controller's graveyard")
    void thresholdGrantsBoostAndFlying() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new MysticCrusader());

        assertStats(3, 2);
        assertThat(gqs.hasKeyword(gd, findCrusader(), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardCards(7));
        harness.addToBattlefield(player1, new MysticCrusader());

        assertStats(2, 1);
        assertThat(gqs.hasKeyword(gd, findCrusader(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses threshold abilities below seven cards")
    void losesThresholdAbilitiesBelowSevenCards() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addToBattlefield(player1, new MysticCrusader());
        assertStats(3, 2);
        assertThat(gqs.hasKeyword(gd, findCrusader(), Keyword.FLYING)).isTrue();

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertStats(2, 1);
        assertThat(gqs.hasKeyword(gd, findCrusader(), Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot be targeted by a red spell")
    void cannotBeTargetedByRedSpell() {
        Permanent crusader = addCrusader(player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crusader.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot be targeted by a black spell")
    void cannotBeTargetedByBlackSpell() {
        Permanent crusader = addCrusader(player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crusader.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        return cards;
    }

    private Permanent addCrusader(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MysticCrusader());
    }

    private Permanent findCrusader() {
        return findPermanent(player1, "Mystic Crusader");
    }

    private void assertStats(int power, int toughness) {
        Permanent crusader = findCrusader();
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(toughness);
    }
}
