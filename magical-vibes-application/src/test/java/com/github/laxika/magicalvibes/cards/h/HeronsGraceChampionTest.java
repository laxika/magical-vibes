package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeronsGraceChampionTest extends BaseCardTest {

    @Test
    @DisplayName("ETB boosts other Humans and grants them lifelink")
    void etbBoostsOtherHumansAndGrantsLifelink() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentHuman = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());

        castChampion();

        assertThat(human.getEffectivePower()).isEqualTo(2);
        assertThat(human.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();
        assertThat(nonHuman.getEffectivePower()).isEqualTo(2);
        assertThat(nonHuman.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.LIFELINK)).isFalse();
        assertThat(opponentHuman.getEffectivePower()).isEqualTo(2);
        assertThat(opponentHuman.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentHuman, Keyword.LIFELINK)).isFalse();

        Permanent champion = findPermanent(player1, "Heron's Grace Champion");
        assertThat(champion.getEffectivePower()).isEqualTo(3);
        assertThat(champion.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB boost and lifelink wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());

        castChampion();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(human.getEffectivePower()).isEqualTo(1);
        assertThat(human.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isFalse();
    }

    private void castChampion() {
        harness.setHand(player1, List.of(new HeronsGraceChampion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
