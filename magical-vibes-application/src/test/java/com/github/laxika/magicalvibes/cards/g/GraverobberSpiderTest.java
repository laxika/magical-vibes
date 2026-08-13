package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraverobberSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+X for each creature card in its controller's graveyard")
    void getsBoostForCreatureCardsInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears()));
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
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
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
                .hasMessageContaining("no more than 1 times each turn");
        assertThat(spider.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
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

    private Permanent addReadySpider(Player player) {
        GameData gd = harness.getGameData();
        Permanent spider = new Permanent(new GraverobberSpider());
        spider.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(spider);
        return spider;
    }
}
