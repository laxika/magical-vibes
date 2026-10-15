package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayaFieryNegotiator;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({YotiaDeclaresWar.class, Ornithopter.class, Millstone.class, GrizzlyBears.class, JayaFieryNegotiator.class})
class YotiaDeclaresWarTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a colorless flying Ornithopter token")
    void chapterICreatesOrnithopterToken() {
        addSagaWithLore(0);

        triggerChapter();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Ornithopter");
        assertThat(token.getCard().getPower()).isZero();
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gqs.isArtifact(gd, token)).isTrue();
    }

    @Test
    @DisplayName("Chapter II taps selected artifacts and deals damage equal to the number tapped")
    void chapterIITapsArtifactsAndDealsTheirCountAsDamage() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent invalidTarget = harness.addToBattlefieldAndReturn(player2, new Millstone());
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice tapChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(tapChoice.validIds()).containsExactlyInAnyOrder(firstArtifact.getId(), secondArtifact.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(firstArtifact.getId(), secondArtifact.getId()));

        assertThat(firstArtifact.isTapped()).isTrue();
        assertThat(secondArtifact.isTapped()).isTrue();
        PendingInteraction.PermanentChoice damageTargetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(damageTargetChoice.validPermanentIds()).contains(target.getId());
        assertThat(damageTargetChoice.validPermanentIds()).doesNotContain(invalidTarget.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III animates one artifact you control until end of turn")
    void chapterIIIAnimatesAnArtifactUntilEndOfTurn() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        addSagaWithLore(2);

        triggerChapter();
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(artifact.getId());
        assertThat(targetChoice.validPermanentIds()).doesNotContain(opponentArtifact.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    @DisplayName("Chapter III may be skipped")
    void chapterIIICanBeSkipped() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        addSagaWithLore(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    @DisplayName("Read ahead starts at chapter III without triggering earlier chapters")
    void readAheadStartsAtChapterThree() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.castFromHand(player1, new YotiaDeclaresWar(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "3");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.assertInGraveyard(player1, "Yotia Declares War");
        harness.assertNotOnBattlefield(player1, "Yotia Declares War");
    }

    @Test
    @DisplayName("Tapping zero artifacts still triggers chapter II's targeted ability")
    void chapterIITappingZeroStillTriggers() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Chapter II still triggers its targeted ability without any untapped artifacts")
    void chapterIIWithoutUntappedArtifactsStillTriggers() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        artifact.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Chapter II offers only untapped artifacts controlled by its controller")
    void chapterIIExcludesTappedForeignAndNonartifactPermanents() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new Millstone());
        tapped.tap();
        harness.addToBattlefield(player2, new Millstone());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(eligible.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(eligible.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(eligible.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III changes an artifact creature's base stats and preserves flying")
    void chapterIIIAnimatesExistingArtifactCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addSagaWithLore(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
        assertThat(artifact.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Yotia Declares War");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(3);
        assertThat(artifact.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Chapter II can damage a planeswalker and allows a response to its reflexive ability")
    void chapterIIDamagesPlaneswalker() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent jaya = harness.enterBattlefieldAndReturn(player2, new JayaFieryNegotiator());
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));
        harness.handlePermanentChosen(player1, jaya.getId());

        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(jaya);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new YotiaDeclaresWar());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
