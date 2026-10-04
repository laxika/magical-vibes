package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EstwaldShieldbasher.class)
class EstwaldShieldbasherTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} gives Estwald Shieldbasher indestructible until end of turn")
    void payingManaGrantsIndestructible() {
        Permanent shieldbasher = addCreatureReady(player1, new EstwaldShieldbasher());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, shieldbasher, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Declining the payment does not grant indestructible")
    void decliningPaymentDoesNotGrantIndestructible() {
        Permanent shieldbasher = addCreatureReady(player1, new EstwaldShieldbasher());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, shieldbasher, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible granted by the attack trigger ends at end of turn")
    void indestructibleEndsAtEndOfTurn() {
        Permanent shieldbasher = addCreatureReady(player1, new EstwaldShieldbasher());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.hasKeyword(gd, shieldbasher, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, shieldbasher, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Colored mana can pay the generic attack-trigger cost")
    void coloredManaPaysForIndestructible() {
        Permanent shieldbasher = addCreatureReady(player1, new EstwaldShieldbasher());
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, shieldbasher, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Accepting without mana does not grant indestructible")
    void cannotGainIndestructibleWithoutPaying() {
        Permanent shieldbasher = addCreatureReady(player1, new EstwaldShieldbasher());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, shieldbasher, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The paid trigger grants indestructible only to its attacking source")
    void onlyAttackingCopyGainsIndestructible() {
        Permanent attacker = addCreatureReady(player1, new EstwaldShieldbasher());
        Permanent nonattacker = addCreatureReady(player1, new EstwaldShieldbasher());
        Permanent opposingCopy = addCreatureReady(player2, new EstwaldShieldbasher());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCopy, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
