package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GnarledSage.class, Island.class})
class GnarledSageTest extends BaseCardTest {

    @Test
    @DisplayName("Gnarled Sage gets +0/+2 and vigilance after its controller draws two cards")
    void gainsBonusAfterControllerDrawsTwoCards() {
        Permanent sage = addCreatureReady(player1, new GnarledSage());
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isFalse();

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);

        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isFalse();

        draw(player1);

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's draws do not enable Gnarled Sage")
    void opponentDrawsDoNotEnableBonus() {
        Permanent sage = addCreatureReady(player1, new GnarledSage());

        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        draw(player2);
        draw(player2);

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Draws before Gnarled Sage enters count, and further draws do not stack its bonus")
    void earlierDrawsCountAndThirdDrawDoesNotIncreaseBonus() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        draw(player1);
        draw(player1);

        Permanent sage = harness.enterBattlefieldAndReturn(player1, new GnarledSage());

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isTrue();

        draw(player1);

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Gnarled Sage's bonus ends when the next turn starts")
    void bonusEndsOnNextTurn() {
        Permanent sage = addCreatureReady(player1, new GnarledSage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        draw(player1);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Its controller's draws during an opponent's turn enable only their own Sage")
    void controllerDrawsOnOpponentsTurnEnableOnlyOwnSage() {
        harness.forceActivePlayer(player2);
        Permanent sage = addCreatureReady(player1, new GnarledSage());
        Permanent opposingSage = addCreatureReady(player2, new GnarledSage());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        draw(player1);

        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, opposingSage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opposingSage, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Gnarled Sage attacks without tapping after two draws")
    void enabledVigilancePreventsTappingToAttack() {
        Permanent sage = addCreatureReady(player1, new GnarledSage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        draw(player1);

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(sage.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sage);
    }

    private void draw(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
