package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.o.OrochiSustainer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeshiroTheAnointed.class, OrochiSustainer.class, HumbleBudoka.class})
class SeshiroTheAnointedTest extends BaseCardTest {

    private Permanent addSeshiro(Player player) {
        return addCreatureReady(player, new SeshiroTheAnointed());
    }

    private void runCombatDamage() {
        resolveCombat();
        resolveAllTriggers();
    }

    private int handSize(Player player) {
        return gd.playerHands.get(player.getId()).size();
    }

    @Test
    @DisplayName("Other Snakes you control get +2/+2")
    void boostsOtherOwnSnakes() {
        Permanent snake = addCreatureReady(player1, new OrochiSustainer());
        addSeshiro(player1);

        var bonus = gqs.computeStaticBonus(gd, snake);

        assertThat(bonus.power()).isEqualTo(2);
        assertThat(bonus.toughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Seshiro does not boost himself")
    void doesNotBoostSelf() {
        Permanent seshiro = addSeshiro(player1);

        assertThat(gqs.computeStaticBonus(gd, seshiro).power()).isZero();
    }

    @Test
    @DisplayName("Non-Snake creatures you control are not boosted")
    void doesNotBoostNonSnakes() {
        Permanent nonSnake = addCreatureReady(player1, new HumbleBudoka());
        addSeshiro(player1);

        assertThat(gqs.computeStaticBonus(gd, nonSnake).power()).isZero();
    }

    @Test
    @DisplayName("Snakes an opponent controls are not boosted")
    void doesNotBoostOpponentSnakes() {
        Permanent opponentSnake = addCreatureReady(player2, new OrochiSustainer());
        addSeshiro(player1);

        assertThat(gqs.computeStaticBonus(gd, opponentSnake).power()).isZero();
    }

    @Test
    @DisplayName("Accepting the may ability draws a card when a Snake deals combat damage")
    void snakeCombatDamageDrawsCard() {
        addSeshiro(player1);
        addCreatureReady(player1, new OrochiSustainer()).setAttacking(true);
        int before = handSize(player1);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(handSize(player1)).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("Declining the may ability draws no card")
    void decliningDrawsNothing() {
        addSeshiro(player1);
        addCreatureReady(player1, new OrochiSustainer()).setAttacking(true);
        int before = handSize(player1);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(handSize(player1)).isEqualTo(before);
    }

    @Test
    @DisplayName("A non-Snake dealing combat damage does not trigger the draw")
    void nonSnakeDoesNotTrigger() {
        addSeshiro(player1);
        addCreatureReady(player1, new HumbleBudoka()).setAttacking(true);
        int before = handSize(player1);

        runCombatDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(handSize(player1)).isEqualTo(before);
    }

    @Test
    @DisplayName("Seshiro triggers for himself when he deals combat damage")
    void triggersForItself() {
        addSeshiro(player1).setAttacking(true);
        int before = handSize(player1);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(handSize(player1)).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("Each Snake dealing combat damage creates its own draw trigger")
    void eachSnakeTriggersIndependently() {
        addSeshiro(player1);
        addCreatureReady(player1, new OrochiSustainer()).setAttacking(true);
        addCreatureReady(player1, new OrochiSustainer()).setAttacking(true);
        int before = handSize(player1);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(handSize(player1)).isEqualTo(before + 2);
    }
}
