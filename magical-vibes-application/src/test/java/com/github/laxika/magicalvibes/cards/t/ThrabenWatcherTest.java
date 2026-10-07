package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MarbleGargoyle;
import com.github.laxika.magicalvibes.cards.s.SealOfRemoval;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrabenWatcher.class, MarbleGargoyle.class, SealOfRemoval.class})
class ThrabenWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Other own nontoken creatures get +1/+1 and vigilance")
    void buffsOtherOwnNontokenCreatures() {
        harness.addToBattlefield(player1, new ThrabenWatcher());
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new MarbleGargoyle());

        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not buff itself or own creature tokens")
    void excludesSourceAndTokens() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new ThrabenWatcher());
        Permanent token = harness.addToBattlefieldAndReturn(player1, createTokenCreature("Soldier Token", 1, 1));

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff an opponent's nontoken creature")
    void doesNotBuffOpponentCreature() {
        harness.addToBattlefield(player1, new ThrabenWatcher());
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player2, new MarbleGargoyle());

        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Multiple Watchers boost each other and stack on other nontoken creatures")
    void multipleWatchersStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ThrabenWatcher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ThrabenWatcher());
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new MarbleGargoyle());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The boost and granted vigilance end when the Watcher leaves the battlefield")
    void bonusEndsWhenWatcherLeaves() {
        harness.addToBattlefield(player1, new SealOfRemoval());
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new ThrabenWatcher());
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new MarbleGargoyle());

        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.VIGILANCE)).isTrue();

        harness.activateAbility(player1, 0, null, watcher.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thraben Watcher");
        harness.assertInHand(player1, "Thraben Watcher");
        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A creature granted vigilance attacks without tapping")
    void grantedVigilancePreventsTappingToAttack() {
        harness.addToBattlefield(player1, new ThrabenWatcher());
        Permanent gargoyle = addCreatureReady(player1, new MarbleGargoyle());

        declareAttackers(List.of(1));

        assertThat(gargoyle.isTapped()).isFalse();
    }

    private static Card createTokenCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }
}
