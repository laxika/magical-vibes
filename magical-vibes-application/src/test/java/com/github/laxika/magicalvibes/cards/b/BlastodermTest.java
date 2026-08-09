package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlastodermTest extends BaseCardTest {

    @Test
    @DisplayName("Blastoderm enters with three fade counters")
    void entersWithFadeCounters() {
        harness.setHand(player1, List.of(new Blastoderm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent blastoderm = findPermanent(player1, "Blastoderm");
        assertThat(blastoderm.getCounterCount(CounterType.FADE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fading removes one fade counter during Blastoderm's controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent blastoderm = addCreatureReady(player1, new Blastoderm());
        blastoderm.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(blastoderm.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Blastoderm");
    }

    @Test
    @DisplayName("Fading sacrifices Blastoderm when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addCreatureReady(player1, new Blastoderm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blastoderm");
    }

    @Test
    @DisplayName("Shroud prevents an opponent's spell from targeting Blastoderm")
    void shroudPreventsTargeting() {
        Permanent blastoderm = addCreatureReady(player1, new Blastoderm());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, blastoderm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
