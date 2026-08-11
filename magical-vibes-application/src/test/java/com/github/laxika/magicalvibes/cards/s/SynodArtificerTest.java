package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynodArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 taps two target noncreature artifacts")
    void tapsXNoncreatureArtifacts() {
        addReadyArtificer();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(artifactCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("X=2 untaps two target noncreature artifacts")
    void untapsXNoncreatureArtifacts() {
        addReadyArtificer();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        first.tap();
        second.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Artifact creatures are illegal targets")
    void rejectsArtifactCreatureTarget() {
        addReadyArtificer();
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(artifactCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArtificer() {
        Permanent artificer = harness.addToBattlefieldAndReturn(player1, new SynodArtificer());
        artificer.setSummoningSick(false);
        return artificer;
    }
}
