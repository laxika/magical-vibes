package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClockworkDrawbridge.class, GrizzlyBears.class, Forest.class})
class ClockworkDrawbridgeTest extends BaseCardTest {

    @Test
    @DisplayName("{2}{W}, {T} taps target creature")
    void abilityTapsTargetCreature() {
        Permanent drawbridge = addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(drawbridge.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
