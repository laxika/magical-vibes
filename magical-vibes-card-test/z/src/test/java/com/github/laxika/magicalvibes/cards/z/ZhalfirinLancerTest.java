package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CopperHostCrusher;
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

@CardUsed({ZhalfirinLancer.class, CopperHostCrusher.class})
class ZhalfirinLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Another Knight entering gives it +1/+1 and vigilance until end of turn")
    void knightEnteringGivesBoostAndVigilance() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ZhalfirinLancer());

        castAnotherLancer();
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

        castCopperHostCrusher();
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
        castAnotherLancer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.VIGILANCE)).isFalse();
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

    @Test
    @DisplayName("An opponent's Knight entering does not trigger it")
    void opposingKnightDoesNotTrigger() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ZhalfirinLancer());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ZhalfirinLancer()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Each Knight entering gives a separate cumulative boost")
    void multipleKnightsGiveCumulativeBoosts() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ZhalfirinLancer());
        castAnotherLancer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        castAnotherLancer();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The Knight that entered stays unboosted and without vigilance")
    void enteringKnightIsNotAffectedByTheTrigger() {
        harness.addToBattlefield(player1, new ZhalfirinLancer());
        castAnotherLancer();
        harness.passBothPriorities();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, entering, Keyword.VIGILANCE)).isFalse();
    }

    private void castCopperHostCrusher() {
        harness.setHand(player1, List.of(new CopperHostCrusher()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.castCreature(player1, 0);
    }

    private void castAnotherLancer() {
        harness.setHand(player1, List.of(new ZhalfirinLancer()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }
}
