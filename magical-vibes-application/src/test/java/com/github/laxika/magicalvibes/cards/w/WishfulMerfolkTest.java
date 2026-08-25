package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WishfulMerfolk.class)
class WishfulMerfolkTest extends BaseCardTest {

    @Test
    void abilityRemovesDefenderAndBecomesHumanUntilEndOfTurn() {
        Permanent merfolk = addCreatureReady(player1, new WishfulMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(merfolk.hasKeyword(Keyword.DEFENDER)).isFalse();
        assertThat(merfolk.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.HUMAN);
    }

    @Test
    void abilityEffectsWearOffAtEndOfTurn() {
        Permanent merfolk = addCreatureReady(player1, new WishfulMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(merfolk.hasKeyword(Keyword.DEFENDER)).isTrue();
        assertThat(merfolk.getTransientCreatureTypeOverride()).isNull();
    }
}
