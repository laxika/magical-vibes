package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Abomination;
import com.github.laxika.magicalvibes.cards.d.Darkness;
import com.github.laxika.magicalvibes.cards.p.Pyrotechnics;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reverberation.class, Pyrotechnics.class, Abomination.class, Darkness.class})
class ReverberationTest extends BaseCardTest {

    @Test
    void redirectsAllDamageFromTargetSorceryToItsController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Abomination());
        Pyrotechnics pyrotechnics = new Pyrotechnics();
        harness.setHand(player1, List.of(pyrotechnics));
        harness.setHand(player2, List.of(new Reverberation()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, Map.of(creature.getId(), 2, player2.getId(), 2));
        harness.castInstant(player2, 0, pyrotechnics.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    void onlyTargetsSorcerySpells() {
        Darkness darkness = new Darkness();
        harness.setHand(player1, List.of(darkness));
        harness.setHand(player2, List.of(new Reverberation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, darkness.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery spell");
    }

    @Test
    void redirectionExpiresAtEndOfTurn() {
        Pyrotechnics firstPyrotechnics = new Pyrotechnics();
        harness.setHand(player1, List.of(firstPyrotechnics));
        harness.setHand(player2, List.of(new Reverberation()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 4));
        harness.castInstant(player2, 0, firstPyrotechnics.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new Pyrotechnics()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castSorcery(player2, 0, Map.of(player1.getId(), 4));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }
}
