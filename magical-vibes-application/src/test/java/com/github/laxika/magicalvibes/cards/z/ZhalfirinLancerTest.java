package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({ZhalfirinLancer.class, YouthfulKnight.class, GrizzlyBears.class})
class ZhalfirinLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Another Knight entering gives it +1/+1 and vigilance until end of turn")
    void knightEnteringGivesBoostAndVigilance() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ZhalfirinLancer());

        castYouthfulKnight();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A non-Knight entering does not trigger it")
    void nonKnightEnteringDoesNotTrigger() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ZhalfirinLancer());

        castGrizzlyBears();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void boostAndVigilanceWearOffAtEndOfTurn() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ZhalfirinLancer());
        castYouthfulKnight();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gqs.getEffectivePower(gameData, lancer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gameData, lancer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gameData, lancer, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Its own entry does not trigger it")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new ZhalfirinLancer()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    private void castYouthfulKnight() {
        harness.setHand(player1, List.of(new YouthfulKnight()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }
}
