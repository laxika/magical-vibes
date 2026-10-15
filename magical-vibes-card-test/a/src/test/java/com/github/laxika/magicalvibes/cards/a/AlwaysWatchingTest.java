package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlwaysWatching.class, GrizzlyBears.class, Opalescence.class})
class AlwaysWatchingTest extends BaseCardTest {

    @Test
    void buffsOwnNontokenCreaturesAndGrantsVigilance() {
        harness.addToBattlefield(player1, new AlwaysWatching());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void doesNotBuffOwnCreatureTokens() {
        harness.addToBattlefield(player1, new AlwaysWatching());
        Permanent token = harness.addToBattlefieldAndReturn(player1, createTokenCreature("Soldier Token", 1, 1));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void doesNotBuffOpponentsCreatures() {
        harness.addToBattlefield(player1, new AlwaysWatching());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void multipleCopiesStackAndRemovingTheLastCopyRemovesVigilance() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AlwaysWatching());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AlwaysWatching());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void vigilanceKeepsAnAttackingNontokenCreatureUntapped() {
        harness.addToBattlefield(player1, new AlwaysWatching());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThat(bears.isAttacking()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void bonusFollowsTheCreaturesCurrentController() {
        harness.addToBattlefield(player1, new AlwaysWatching());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void animatedAlwaysWatchingReceivesItsOwnBonusAndVigilance() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent watching = harness.addToBattlefieldAndReturn(player1, new AlwaysWatching());

        assertThat(gqs.isCreature(gd, watching)).isTrue();
        assertThat(gqs.getEffectivePower(gd, watching)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, watching)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, watching, Keyword.VIGILANCE)).isTrue();
    }

    private Card createTokenCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }
}
