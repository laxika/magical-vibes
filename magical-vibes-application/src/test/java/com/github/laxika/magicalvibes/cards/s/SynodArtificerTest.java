package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SynodArtificer.class, DarksteelIngot.class, MyrMoonvessel.class, CrazedGoblin.class})
class SynodArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 taps two target noncreature artifacts")
    void tapsXNoncreatureArtifacts() {
        Permanent artificer = addCreatureReady(player1, new SynodArtificer());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(artifactCreature.isTapped()).isFalse();
        assertThat(artificer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("X=2 untaps two target noncreature artifacts")
    void untapsXNoncreatureArtifacts() {
        Permanent artificer = addCreatureReady(player1, new SynodArtificer());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        first.tap();
        second.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(artificer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Artifact creatures are illegal targets")
    void rejectsArtifactCreatureTarget() {
        addCreatureReady(player1, new SynodArtificer());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(artifactCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact creatures are illegal targets for the untap ability")
    void rejectsArtifactCreatureTargetForUntapAbility() {
        addCreatureReady(player1, new SynodArtificer());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, 1, List.of(artifactCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Nonartifact permanents are illegal targets")
    void rejectsNonartifactTarget() {
        addCreatureReady(player1, new SynodArtificer());
        Permanent nonartifact = harness.addToBattlefieldAndReturn(player2, new CrazedGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(nonartifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 allows the tap ability without targets")
    void allowsZeroTargets() {
        Permanent artificer = addCreatureReady(player1, new SynodArtificer());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(artificer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap ability requires exactly X targets")
    void tapAbilityRequiresExactlyXTargets() {
        addCreatureReady(player1, new SynodArtificer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The untap ability requires exactly X targets")
    void untapAbilityRequiresExactlyXTargets() {
        addCreatureReady(player1, new SynodArtificer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
