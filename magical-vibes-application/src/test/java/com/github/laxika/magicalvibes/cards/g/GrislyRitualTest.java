package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrislyRitual.class, GrizzlyBears.class, JaceBeleren.class, Plains.class})
class GrislyRitualTest extends BaseCardTest {

    @Test
    void destroysCreatureAndCreatesTwoBloodTokens() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
    }

    @Test
    void destroysPlaneswalkerAndCreatesTwoBloodTokens() {
        Permanent jace = addReadyJace();

        cast(jace.getId());

        harness.assertInGraveyard(player2, "Jace Beleren");
        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
    }

    @Test
    void cannotTargetNonCreatureNonPlaneswalker() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new GrislyRitual()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(harness.getPermanentId(player2, "Plains"))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new GrislyRitual()));
        addMana();
        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private Permanent addReadyJace() {
        Permanent jace = new Permanent(new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        jace.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(jace);
        return jace;
    }
}
