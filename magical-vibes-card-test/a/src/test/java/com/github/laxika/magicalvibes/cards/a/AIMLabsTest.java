package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AIMLabs.class)
class AIMLabsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield tapped gains 1 life")
    void entersTappedAndGainsOneLife() {
        harness.setHand(player1, List.of(new AIMLabs()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent labs = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(labs.isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        Permanent labs = addLabsReady();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(labs.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        Permanent labs = addLabsReady();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(labs.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain waits for the enter trigger to resolve")
    void lifeGainUsesTheStack() {
        harness.setHand(player1, List.of(new AIMLabs()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the entering land's controller gains life")
    void opponentControllerGainsLife() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AIMLabs()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("A newly controlled untapped land produces mana without using the stack")
    void manaAbilityResolvesImmediatelyWithoutSummoningSicknessRestriction() {
        Permanent labs = harness.addToBattlefieldAndReturn(player1, new AIMLabs());
        labs.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(labs.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    private Permanent addLabsReady() {
        Permanent labs = harness.addToBattlefieldAndReturn(player1, new AIMLabs());
        labs.setSummoningSick(false);
        return labs;
    }
}
