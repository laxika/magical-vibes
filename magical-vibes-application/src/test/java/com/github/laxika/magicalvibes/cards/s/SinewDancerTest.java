package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SinewDancerTest extends BaseCardTest {

    @Test
    @DisplayName("The regular ability taps a target creature")
    void regularAbilityTapsTargetCreature() {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(dancer.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The corrupted ability taps a target creature when an opponent has three poison counters")
    void corruptedAbilityTapsTargetCreatureWhenOpponentHasThreePoisonCounters() {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(dancer.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The corrupted ability cannot be activated without three poison counters on an opponent")
    void corruptedAbilityRequiresThreePoisonCounters() {
        Permanent dancer = addCreatureReady(player1, new SinewDancer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dancer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new SinewDancer());
        Permanent target = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
