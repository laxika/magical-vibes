package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.j.JanglingAutomaton;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaraxusOfKeld.class, WindingCanyons.class, MindStone.class, RedwoodTreefolk.class,
        JanglingAutomaton.class})
class MaraxusOfKeldTest extends BaseCardTest {

    @Test
    @DisplayName("Maraxus counts untapped artifacts, creatures and lands you control, including itself")
    void countsUntappedPermanents() {
        Permanent maraxus = addMaraxusReady(player1);
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new RedwoodTreefolk());
        harness.addToBattlefield(player1, new JanglingAutomaton());

        // 2 lands + 1 artifact + 1 creature + 1 artifact creature + Maraxus itself.
        assertThat(gqs.getEffectivePower(gd, maraxus)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, maraxus)).isEqualTo(6);
    }

    @Test
    @DisplayName("Maraxus does not count tapped permanents")
    void ignoresTappedPermanents() {
        Permanent maraxus = addMaraxusReady(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WindingCanyons());
        harness.addToBattlefield(player1, new WindingCanyons());

        assertThat(gqs.getEffectivePower(gd, maraxus)).isEqualTo(3);

        land.tap();
        assertThat(gqs.getEffectivePower(gd, maraxus)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, maraxus)).isEqualTo(2);

        maraxus.tap();
        assertThat(gqs.getEffectivePower(gd, maraxus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, maraxus)).isEqualTo(1);
    }

    @Test
    @DisplayName("Maraxus does not count permanents your opponent controls")
    void ignoresOpponentPermanents() {
        Permanent maraxus = addMaraxusReady(player1);
        harness.addToBattlefield(player2, new WindingCanyons());
        harness.addToBattlefield(player2, new RedwoodTreefolk());

        assertThat(gqs.getEffectivePower(gd, maraxus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, maraxus)).isEqualTo(1);
    }

    @Test
    @DisplayName("Maraxus resolves from the stack and sizes itself on the battlefield")
    void resolvesFromStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.castFromHand(player1, new MaraxusOfKeld(), "{4}{R}{R}");
        harness.passBothPriorities();

        Permanent maraxus = findPermanent(player1, "Maraxus of Keld");
        assertThat(gqs.getEffectivePower(gd, maraxus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, maraxus)).isEqualTo(3);
    }

    private Permanent addMaraxusReady(Player player) {
        return addCreatureReady(player, new MaraxusOfKeld());
    }
}
