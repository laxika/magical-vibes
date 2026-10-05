package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoodmarkPainter.class, GrizzlyBears.class, LightningBolt.class, FountainOfYouth.class})
class MoodmarkPainterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature menace and +X/+0 for creature cards in the controller's graveyard")
    void etbScalesBoostAndGrantsMenace() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt()));
        harness.setHand(player1, List.of(new MoodmarkPainter()));
        addMana();

        gs.playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("The boost counts only creature cards in the controller's graveyard")
    void countsOnlyControllerCreatureCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new MoodmarkPainter()));
        addMana();

        gs.playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost and menace wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new MoodmarkPainter()));
        addMana();

        gs.playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new MoodmarkPainter()));
        addMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({MoodmarkPainter.class})
    @DisplayName("An empty graveyard still grants menace to a friendly creature")
    void emptyGraveyardStillGrantsMenace() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MoodmarkPainter());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new MoodmarkPainter()));
        addMana();

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @CardUsed({MoodmarkPainter.class})
    @DisplayName("Undergrowth counts the graveyard at resolution and then fixes the boost")
    void countsAtResolutionAndDoesNotRecalculate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoodmarkPainter());
        harness.setGraveyard(player1, List.of(new MoodmarkPainter()));
        harness.setHand(player1, List.of(new MoodmarkPainter()));
        addMana();

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new MoodmarkPainter(), new MoodmarkPainter()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(target.getGrantedKeywords()).contains(Keyword.MENACE);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
    }

    @Test
    @CardUsed({MoodmarkPainter.class, LightningBolt.class})
    @DisplayName("The trigger survives the Painter dying and counts it in the graveyard")
    void triggerResolvesAfterPainterDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoodmarkPainter());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new MoodmarkPainter(), new LightningBolt()));
        addMana();

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        UUID painterId = harness.getPermanentId(player1, "Moodmark Painter");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, painterId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Moodmark Painter");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @CardUsed({MoodmarkPainter.class, LightningBolt.class})
    @DisplayName("Removing the only target prevents both parts of the trigger from resolving")
    void removedTargetDoesNotRedirectToPainter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoodmarkPainter());
        harness.setGraveyard(player1, List.of(new MoodmarkPainter()));
        harness.setHand(player1, List.of(new MoodmarkPainter(), new LightningBolt()));
        addMana();

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent painter = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Moodmark Painter");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, painter)).isEqualTo(2);
        assertThat(painter.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
