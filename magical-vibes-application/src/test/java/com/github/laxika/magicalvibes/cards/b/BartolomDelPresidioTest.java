package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BartolomDelPresidio.class, GrizzlyBears.class, Spellbook.class})
class BartolomDelPresidioTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Bartolomé del Presidio")
    void sacrificingAnotherCreaturePutsCounter() {
        Permanent bartolom = addCreatureReady(player1, new BartolomDelPresidio());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bartolom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bartolom.getEffectivePower()).isEqualTo(3);
        assertThat(bartolom.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing another artifact puts a +1/+1 counter on Bartolomé del Presidio")
    void sacrificingAnotherArtifactPutsCounter() {
        Permanent bartolom = addCreatureReady(player1, new BartolomDelPresidio());
        harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bartolom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bartolom.getEffectivePower()).isEqualTo(3);
        assertThat(bartolom.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Bartolomé del Presidio itself")
    void activatedAbilityRequiresAnotherPermanent() {
        addCreatureReady(player1, new BartolomDelPresidio());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
