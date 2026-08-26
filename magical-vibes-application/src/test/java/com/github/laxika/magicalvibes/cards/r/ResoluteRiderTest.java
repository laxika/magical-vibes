package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResoluteRider.class})
class ResoluteRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Lifelink ability is payable with black mana")
    void gainsLifelink() {
        Permanent rider = addCreatureReady(player1, new ResoluteRider());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rider, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Indestructible ability is payable with white mana")
    void gainsIndestructible() {
        Permanent rider = addCreatureReady(player1, new ResoluteRider());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rider, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Lifelink and indestructible wear off at end of turn")
    void temporaryAbilitiesWearOffAtEndOfTurn() {
        Permanent rider = addCreatureReady(player1, new ResoluteRider());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rider, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, rider, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rider, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, rider, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
