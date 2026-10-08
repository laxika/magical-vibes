package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BattleMammoth;
import com.github.laxika.magicalvibes.cards.l.LittjaraKinseekers;
import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.cards.r.RavenousLindwurm;
import com.github.laxika.magicalvibes.cards.s.SnakeskinVeil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheBearsOfLittjara.class, Mistwalker.class, LittjaraKinseekers.class,
        RavenousLindwurm.class, SnakeskinVeil.class, BattleMammoth.class, TyvarKell.class})
class TheBearsOfLittjaraTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a blue 2/2 Shapeshifter with changeling")
    void chapterICreatesShapeshifter() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Shapeshifter");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().hasKeyword(Keyword.CHANGELING)).isTrue();
    }

    @Test
    @DisplayName("Chapter II sets any number of your Shapeshifters to 4/4 indefinitely")
    void chapterIISetsChosenShapeshiftersIndefinitely() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Mistwalker());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LittjaraKinseekers());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RavenousLindwurm());
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(first.getId(), second.getId()).doesNotContain(other.getId());

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Chapter III lets creatures with power 4 or greater damage a creature or planeswalker")
    void chapterIIIDamagesPlaneswalkerWithLargeCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        harness.addToBattlefield(player1, new RavenousLindwurm());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TyvarKell());
        planeswalker.setCounterCount(CounterType.LOYALTY, 8);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(planeswalker.getId());

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void chapterIICanChooseOnlyOneShapeshifterAndKeepsCounters() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new Mistwalker());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new LittjaraKinseekers());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Mistwalker());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(chosen.getId(), unchosen.getId()).doesNotContain(opponent.getId());
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, chosen)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, unchosen)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
    }

    @Test
    void chapterIICanChooseZeroTargets() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        Permanent shapeshifter = harness.addToBattlefieldAndReturn(player1, new Mistwalker());
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, shapeshifter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shapeshifter)).isEqualTo(4);
    }

    @Test
    void chapterIIICountsAllYourLargeCreaturesAndExcludesSmallAndOpposingCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        Permanent threshold = harness.addToBattlefieldAndReturn(player1, new Mistwalker());
        threshold.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player1, new RavenousLindwurm());
        harness.addToBattlefield(player1, new LittjaraKinseekers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(10);
        assertThat(threshold.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "The Bears of Littjara");
    }

    @Test
    void chapterIIICanChooseZeroTargetsAndStillSacrificesSaga() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        harness.addToBattlefield(player1, new RavenousLindwurm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mistwalker());
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "The Bears of Littjara");
    }

    @Test
    void chapterIIIDoesNotOfferOpposingHexproofCreatureAsTarget() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new Mistwalker());
        harness.setHand(player2, List.of(new SnakeskinVeil()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, protectedCreature.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(legalTarget.getId()).doesNotContain(protectedCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
    }

    @Test
    void chapterIIITargetingTriggersBattleMammothBeforeDamage() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        harness.addToBattlefield(player1, new RavenousLindwurm());
        harness.addToBattlefield(player2, new BattleMammoth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mistwalker());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new LittjaraKinseekers()));
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(target.getMarkedDamage()).isZero();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    void chapterIIIChecksPowerWhenTheAbilityResolves() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new Mistwalker());
        hunter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());
        harness.setHand(player1, List.of(new SnakeskinVeil()));
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, hunter.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void enteringSagaTriggersChapterIWithoutWaitingForDrawStep() {
        harness.setHand(player1, List.of(new TheBearsOfLittjara()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Shapeshifter")).isEqualTo(1);
        assertThat(findPermanent(player1, "The Bears of Littjara").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
    }

    @Test
    void chapterIIIDoesNotDamageTargetThatGainsHexproofInResponse() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        harness.addToBattlefield(player1, new RavenousLindwurm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());
        harness.setHand(player2, List.of(new SnakeskinVeil()));
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "The Bears of Littjara");
    }

    @Test
    void chapterIIICanTargetYourOwnCreatureAndItDealsDamageToItself() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RavenousLindwurm());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ravenous Lindwurm");
        harness.assertNotOnBattlefield(player1, "The Bears of Littjara");
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
