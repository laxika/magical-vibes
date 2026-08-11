package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PurphorosGodOfTheForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Purphoros is not a creature below five devotion to red")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent purphoros = addPurphoros();
        addRedPermanents(3);

        assertThat(gqs.isCreature(gd, purphoros)).isFalse();
        assertThat(gqs.isEnchantment(gd, purphoros)).isTrue();
    }

    @Test
    @DisplayName("Purphoros becomes a creature at five devotion to red")
    void becomesCreatureAtDevotionThreshold() {
        Permanent purphoros = addPurphoros();
        addRedPermanents(4);

        assertThat(gqs.isCreature(gd, purphoros)).isTrue();
    }

    @Test
    @DisplayName("Another creature entering under your control deals 2 damage to each opponent")
    void damagesEachOpponentWhenAllyCreatureEnters() {
        harness.setLife(player2, 20);
        addPurphoros();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A creature entering under an opponent's control does not trigger Purphoros")
    void doesNotTriggerForOpponentCreature() {
        harness.setLife(player2, 20);
        addPurphoros();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Paying {2}{R} gives creatures you control +1/+0 until end of turn")
    void boostsOwnCreaturesUntilEndOfTurn() {
        Permanent purphoros = addPurphoros();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, purphoros)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    private Permanent addPurphoros() {
        return harness.addToBattlefieldAndReturn(player1, new PurphorosGodOfTheForge());
    }

    private void addRedPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new RagingGoblin());
        }
    }
}
