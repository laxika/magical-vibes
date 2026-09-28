package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PincerSpider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightscapeApprentice.class, PincerSpider.class, Mountain.class})
class NightscapeApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("{U}, {T}: puts target creature you control on top of its owner's library")
    void tucksControlledCreature() {
        addCreatureReady(player1, new NightscapeApprentice());
        Permanent spider = addCreatureReady(player1, new PincerSpider());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, spider.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pincer Spider"));
        assertThat(gd.playerDecks.get(player1.getId()).get(0).getName())
                .isEqualTo("Pincer Spider");
    }

    @Test
    @DisplayName("The tuck ability cannot target a creature you don't control")
    void tuckRejectsOpponentCreature() {
        addCreatureReady(player1, new NightscapeApprentice());
        Permanent spider = addCreatureReady(player2, new PincerSpider());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, spider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("{R}, {T}: target creature gains first strike until end of turn")
    void grantsFirstStrike() {
        addCreatureReady(player1, new NightscapeApprentice());
        Permanent spider = addCreatureReady(player2, new PincerSpider());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, spider.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spider, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The first-strike ability cannot target a noncreature permanent")
    void firstStrikeRejectsNoncreaturePermanent() {
        addCreatureReady(player1, new NightscapeApprentice());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        addCreatureReady(player1, new NightscapeApprentice());
        Permanent spider = addCreatureReady(player1, new PincerSpider());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, spider.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spider, Keyword.FIRST_STRIKE)).isFalse();
    }
}
