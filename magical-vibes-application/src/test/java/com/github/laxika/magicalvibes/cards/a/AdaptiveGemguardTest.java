package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdaptiveGemguard.class, MoxOpal.class, GrizzlyBears.class, Forest.class})
class AdaptiveGemguardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the source and an artifact puts a +1/+1 counter on Adaptive Gemguard")
    void tapsArtifactAndPutsCounterOnSource() {
        Permanent gemguard = addReady(player1, new AdaptiveGemguard());
        Permanent artifact = addReady(player1, new MoxOpal());

        activate(gemguard);

        assertThat(gemguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gemguard.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping the source and a creature satisfies the artifact-or-creature cost")
    void tapsCreatureAndPutsCounterOnSource() {
        Permanent gemguard = addReady(player1, new AdaptiveGemguard());
        Permanent creature = addReady(player1, new GrizzlyBears());

        activate(gemguard);

        assertThat(gemguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gemguard.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without two qualifying untapped permanents")
    void cannotActivateWithoutTwoQualifyingPermanents() {
        Permanent gemguard = addReady(player1, new AdaptiveGemguard());
        Permanent land = addReady(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gemguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gemguard.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        Permanent gemguard = addReady(player1, new AdaptiveGemguard());
        Permanent artifact = addReady(player1, new MoxOpal());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(gemguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gemguard.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void activate(Permanent gemguard) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gemguard), null, null);
        harness.passBothPriorities();
    }
}
