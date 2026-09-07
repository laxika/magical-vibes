package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheTriumphOfAnax.class, GrizzlyBears.class, HillGiant.class})
class TheTriumphOfAnaxTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I grants trample and power equal to lore counters")
    void chapterIGrantsTrampleAndLorePower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheTriumphOfAnax()));
        addSagaMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Chapter II uses the current lore counter count")
    void chapterIIUsesCurrentLoreCount() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheTriumphOfAnax());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapterTarget();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Chapter III uses three lore counters")
    void chapterIIIUsesThreeLoreCounters() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheTriumphOfAnax());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapterTarget();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Chapter IV fights a creature you control against an optional opposing target")
    void chapterIVFightsChosenCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheTriumphOfAnax());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 3);

        advanceToNextChapterTarget();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opposingCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opposingCreature.getId(), player1.getId())
                .doesNotContain(ownCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "The Triumph of Anax");
    }

    @Test
    @DisplayName("Chapter IV can decline its optional opposing target")
    void chapterIVCanDeclineOpposingTarget() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheTriumphOfAnax());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 3);

        advanceToNextChapterTarget();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void addSagaMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void advanceToNextChapterTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
