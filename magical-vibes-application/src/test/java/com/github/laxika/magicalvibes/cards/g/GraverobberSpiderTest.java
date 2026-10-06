package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MortalsArdor;
import com.github.laxika.magicalvibes.cards.s.SatyrWayfinder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraverobberSpider.class, SatyrWayfinder.class, MortalsArdor.class})
class GraverobberSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+X for each creature card in its controller's graveyard")
    void getsBoostForCreatureCardsInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new SatyrWayfinder(), new MortalsArdor(), new SatyrWayfinder()));
        Permanent spider = addReadySpider(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spider.getEffectivePower()).isEqualTo(4);
        assertThat(spider.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Ignores noncreature cards and cards in an opponent's graveyard")
    void ignoresNoncreatureAndOpponentGraveyardCards() {
        harness.setGraveyard(player1, List.of(new MortalsArdor()));
        harness.setGraveyard(player2, List.of(new SatyrWayfinder(), new SatyrWayfinder()));
        Permanent spider = addReadySpider(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spider.getEffectivePower()).isEqualTo(2);
        assertThat(spider.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can be activated only once each turn")
    void canBeActivatedOnlyOnceEachTurn() {
        Permanent spider = addReadySpider(player1);
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(spider.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new SatyrWayfinder()));
        Permanent spider = addReadySpider(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(spider.getEffectivePower()).isEqualTo(3);
        assertThat(spider.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spider.getEffectivePower()).isEqualTo(2);
        assertThat(spider.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts creatures at resolution and keeps that boost fixed afterward")
    void countsAtResolutionAndKeepsBoostFixed() {
        Permanent spider = addReadySpider(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, null, null);

        harness.setGraveyard(player1, List.of(new SatyrWayfinder(), new SatyrWayfinder()));
        harness.passBothPriorities();
        assertThat(spider.getEffectivePower()).isEqualTo(4);
        assertThat(spider.getEffectiveToughness()).isEqualTo(6);

        harness.setGraveyard(player1, List.of());
        assertThat(spider.getEffectivePower()).isEqualTo(4);
        assertThat(spider.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot activate again while the first activation is still on the stack")
    void cannotActivateAgainBeforeResolution() {
        addReadySpider(player1);
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        harness.setGraveyard(player1, List.of(new SatyrWayfinder()));
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GraverobberSpider());
        spider.setSummoningSick(true);
        spider.tap();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spider.getEffectivePower()).isEqualTo(3);
        assertThat(spider.getEffectiveToughness()).isEqualTo(5);
        assertThat(spider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate again on the opponent's next turn")
    void canActivateAgainOnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new SatyrWayfinder()));
        Permanent spider = addReadySpider(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(spider.getEffectivePower()).isEqualTo(2);
        assertThat(spider.getEffectiveToughness()).isEqualTo(4);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spider.getEffectivePower()).isEqualTo(3);
        assertThat(spider.getEffectiveToughness()).isEqualTo(5);
    }

    private Permanent addReadySpider(Player player) {
        Permanent spider = harness.addToBattlefieldAndReturn(player, new GraverobberSpider());
        spider.setSummoningSick(false);
        return spider;
    }
}
