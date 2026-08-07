package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaraxusOfKeldTest extends BaseCardTest {

    @Test
    @DisplayName("Maraxus counts untapped artifacts, creatures and lands you control, including itself")
    void countsUntappedPermanents() {
        Permanent maraxus = addMaraxusReady(player1);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());

        // 2 lands + 1 artifact + Grizzly Bears + Maraxus itself.
        assertThat(gqs.getEffectivePower(gd, maraxus)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, maraxus)).isEqualTo(5);
    }

    @Test
    @DisplayName("Maraxus does not count tapped permanents")
    void ignoresTappedPermanents() {
        Permanent maraxus = addMaraxusReady(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, maraxus)).isEqualTo(3);

        mountain.tap();
        assertThat(gqs.getEffectivePower(gd, maraxus)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, maraxus)).isEqualTo(2);
    }

    @Test
    @DisplayName("Maraxus does not count permanents your opponent controls")
    void ignoresOpponentPermanents() {
        Permanent maraxus = addMaraxusReady(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, maraxus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, maraxus)).isEqualTo(1);
    }

    @Test
    @DisplayName("Maraxus resolves from the stack and sizes itself on the battlefield")
    void resolvesFromStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setHand(player1, List.of(new MaraxusOfKeld()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent maraxus = findPermanent(player1, "Maraxus of Keld");
        assertThat(gqs.getEffectivePower(gd, maraxus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, maraxus)).isEqualTo(3);
    }

    private Permanent addMaraxusReady(Player player) {
        return addCreatureReady(player, new MaraxusOfKeld());
    }
}
