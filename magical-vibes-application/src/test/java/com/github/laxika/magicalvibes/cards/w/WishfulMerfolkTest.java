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

@CardUsed({WishfulMerfolk.class})
class WishfulMerfolkTest extends BaseCardTest {

    @Test
    void abilityRemovesDefenderAndBecomesHumanUntilEndOfTurn() {
        Permanent merfolk = addCreatureReady(player1, new WishfulMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(merfolk.hasKeyword(Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, merfolk, CardSubtype.HUMAN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, merfolk, CardSubtype.MERFOLK)).isFalse();
        assertThat(als.canAttack(gd, merfolk, player1.getId())).isTrue();
    }

    @Test
    void abilityEffectsWearOffAtEndOfTurn() {
        Permanent merfolk = addCreatureReady(player1, new WishfulMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(merfolk.hasKeyword(Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, merfolk, CardSubtype.HUMAN)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, merfolk, CardSubtype.MERFOLK)).isTrue();
        assertThat(als.canAttack(gd, merfolk, player1.getId())).isFalse();
    }

    @Test
    void abilityOnlyChangesItsSourceAndDoesNotApplyBeforeResolution() {
        Permanent merfolk = addCreatureReady(player1, new WishfulMerfolk());
        Permanent other = addCreatureReady(player1, new WishfulMerfolk());
        Permanent opponent = addCreatureReady(player2, new WishfulMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(als.canAttack(gd, merfolk, player1.getId())).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, merfolk, CardSubtype.HUMAN)).isFalse();

        harness.passBothPriorities();

        assertThat(als.canAttack(gd, merfolk, player1.getId())).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, merfolk, CardSubtype.HUMAN)).isTrue();
        assertThat(other.hasKeyword(Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.HUMAN)).isFalse();
        assertThat(opponent.hasKeyword(Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, opponent, CardSubtype.HUMAN)).isFalse();
    }

    @Test
    void abilityCanBeActivatedRepeatedlyWhileTappedAndSummoningSick() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new WishfulMerfolk());
        merfolk.setTapped(true);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(merfolk.hasKeyword(Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, merfolk, CardSubtype.HUMAN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, merfolk, CardSubtype.MERFOLK)).isFalse();
    }
}
