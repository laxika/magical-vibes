package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GourmandsTalent.class, Clue.class, GrizzlyBears.class})
class GourmandsTalentTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, your artifacts are Foods and can be sacrificed for life")
    void artifactsBecomeFoodDuringYourTurn() {
        harness.addToBattlefield(player1, new GourmandsTalent());
        Permanent clue = harness.addToBattlefieldAndReturn(player1, new Clue());

        assertThat(gqs.hasEffectiveSubtype(gd, clue, CardSubtype.FOOD)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasEffectiveSubtype(gd, clue, CardSubtype.FOOD)).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(clue);
    }

    @Test
    @DisplayName("At level 2, creates a Raccoon the first time you gain life each turn")
    void levelTwoCreatesRaccoonOncePerTurn() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new GourmandsTalent());
        levelUpToTwo(talent);

        gainLife(1);
        resolveAllTriggers();
        gainLife(1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Raccoon")).isEqualTo(1);
    }

    @Test
    @DisplayName("At level 3, also puts a +1/+1 counter on each creature you control")
    void levelThreeAddsCountersToControlledCreatures() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new GourmandsTalent());
        levelUpToTwo(talent);
        levelUpToThree(talent);
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        gainLife(2);
        resolveAllTriggers();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Raccoon")).isEqualTo(1);
    }

    private void levelUpToTwo(Permanent talent) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(talent), 0, null, null);
        resolveAllTriggers();
    }

    private void levelUpToThree(Permanent talent) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(talent), 1, null, null);
        resolveAllTriggers();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void gainLife(int amount) {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), amount));
    }
}
