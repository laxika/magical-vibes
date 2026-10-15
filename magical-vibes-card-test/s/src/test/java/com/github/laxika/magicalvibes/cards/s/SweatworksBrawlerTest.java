package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherChaser;
import com.github.laxika.magicalvibes.cards.h.HopeOfGhirapur;
import com.github.laxika.magicalvibes.cards.i.ImplementOfCombustion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SweatworksBrawler.class, HopeOfGhirapur.class, ImplementOfCombustion.class, AetherChaser.class})
class SweatworksBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Improvise taps an artifact to pay generic mana")
    void improviseTapsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ImplementOfCombustion());
        harness.setHand(player1, List.of(new SweatworksBrawler()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sweatworks Brawler");
    }

    @Test
    @DisplayName("Improvise cannot tap a nonartifact permanent")
    void improviseRejectsNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AetherChaser());
        harness.setHand(player1, List.of(new SweatworksBrawler()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not an artifact");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Menace requires at least two blockers")
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new SweatworksBrawler());
        addCreatureReady(player2, new AetherChaser());
        addCreatureReady(player2, new AetherChaser());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void improviseCanTapSummoningSickArtifactCreatureWithInsufficientManaAlone() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HopeOfGhirapur());
        artifact.setSummoningSick(true);
        harness.setHand(player1, List.of(new SweatworksBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Sweatworks Brawler");
    }

    @Test
    void improviseCannotPayRedMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HopeOfGhirapur());
        harness.setHand(player1, List.of(new SweatworksBrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).doesNotContain(0);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Sweatworks Brawler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improviseRejectsTappedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HopeOfGhirapur());
        artifact.tap();
        harness.setHand(player1, List.of(new SweatworksBrawler()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertInHand(player1, "Sweatworks Brawler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improviseRejectsRepeatedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HopeOfGhirapur());
        harness.setHand(player1, List.of(new SweatworksBrawler()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Sweatworks Brawler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improviseCannotTapOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HopeOfGhirapur());
        harness.setHand(player1, List.of(new SweatworksBrawler()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Sweatworks Brawler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void menaceAllowsNoBlockers() {
        addCreatureReady(player1, new SweatworksBrawler());
        addCreatureReady(player2, new AetherChaser());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }
}
