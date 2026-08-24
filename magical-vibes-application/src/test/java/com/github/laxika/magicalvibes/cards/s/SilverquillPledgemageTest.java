package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
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

class SilverquillPledgemageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant lets Silverquill Pledgemage gain flying")
    void castingInstantGrantsFlying() {
        Permanent pledgemage = addCreatureReady(player1, new SilverquillPledgemage());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, pledgemage.getId());
        resolveUntilChoice();
        harness.handleListChoice(player1, "FLYING");

        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Copying an instant lets Silverquill Pledgemage gain lifelink")
    void copyingInstantGrantsLifelink() {
        Permanent pledgemage = addCreatureReady(player1, new SilverquillPledgemage());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, pledgemage.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveUntilChoice();
        harness.handleListChoice(player1, "LIFELINK");
        resolveUntilChoice();
        harness.handleListChoice(player1, "LIFELINK");

        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The magecraft keyword wears off at end of turn")
    void keywordWearsOffAtEndOfTurn() {
        Permanent pledgemage = addCreatureReady(player1, new SilverquillPledgemage());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, pledgemage.getId());
        resolveUntilChoice();
        harness.handleListChoice(player1, "FLYING");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, pledgemage, Keyword.LIFELINK)).isFalse();
    }

    private void resolveUntilChoice() {
        int guard = 0;
        while (!gd.interaction.isAwaitingInput() && !gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }
}
