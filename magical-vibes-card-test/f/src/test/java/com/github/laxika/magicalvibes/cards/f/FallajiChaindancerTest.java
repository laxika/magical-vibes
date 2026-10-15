package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallajiChaindancer.class})
class FallajiChaindancerTest extends BaseCardTest {

    @Test
    void activatingAbilityGrantsDoubleStrikeUntilEndOfTurn() {
        Permanent chaindancer = addChaindancerReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, chaindancer, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, chaindancer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void cannotActivateAbilityWithoutTwoMana() {
        addChaindancerReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void canActivateWhileTappedAndSummoningSickUsingColoredMana() {
        Permanent chaindancer = addChaindancerReady(player1);
        chaindancer.setSummoningSick(true);
        chaindancer.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, chaindancer, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, chaindancer, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(chaindancer.isTapped()).isTrue();
    }

    @Test
    void abilityOnlyGrantsDoubleStrikeToItsSource() {
        Permanent chaindancer = addChaindancerReady(player1);
        Permanent other = addChaindancerReady(player1);
        Permanent opponent = addChaindancerReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, chaindancer, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void repeatedActivationsDoNotMultiplyCombatDamage() {
        Permanent chaindancer = addChaindancerReady(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        chaindancer.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private Permanent addChaindancerReady(Player player) {
        return addCreatureReady(player, new FallajiChaindancer());
    }
}
