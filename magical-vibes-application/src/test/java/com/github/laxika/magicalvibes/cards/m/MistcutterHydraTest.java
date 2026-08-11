package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MistcutterHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new MistcutterHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Mistcutter Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot be countered by Cancel")
    void cannotBeCountered() {
        MistcutterHydra hydra = new MistcutterHydra();
        harness.setHand(player1, List.of(hydra));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hydra.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistcutter Hydra");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Has protection from blue")
    void hasProtectionFromBlue() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new MistcutterHydra());

        assertThat(gqs.hasProtectionFrom(gd, hydra, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("Cannot be targeted by a blue spell")
    void cannotBeTargetedByBlueSpell() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new MistcutterHydra());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, hydra.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }
}
