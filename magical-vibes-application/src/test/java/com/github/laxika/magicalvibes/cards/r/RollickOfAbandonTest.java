package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RollickOfAbandonTest extends BaseCardTest {

    @Test
    @DisplayName("Gives +2/-2 to every creature on both battlefields")
    void boostsAllCreatures() {
        harness.addToBattlefield(player1, new GiantSpider()); // 2/4
        harness.addToBattlefield(player2, new GiantSpider()); // 2/4

        harness.setHand(player1, List.of(new RollickOfAbandon()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent own = findPermanent(player1, "Giant Spider");
        Permanent theirs = findPermanent(player2, "Giant Spider");

        assertThat(own.getEffectivePower()).isEqualTo(4);
        assertThat(own.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.getEffectivePower()).isEqualTo(4);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kills creatures with two or less toughness")
    void killsCreaturesWithTwoOrLessToughness() {
        harness.addToBattlefield(player2, new FugitiveWizard()); // 1/1

        harness.setHand(player1, List.of(new RollickOfAbandon()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GiantSpider()); // 2/4

        harness.setHand(player1, List.of(new RollickOfAbandon()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(spider.getEffectivePower()).isEqualTo(4);
        assertThat(spider.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spider.getEffectivePower()).isEqualTo(2);
        assertThat(spider.getEffectiveToughness()).isEqualTo(4);
    }
}
