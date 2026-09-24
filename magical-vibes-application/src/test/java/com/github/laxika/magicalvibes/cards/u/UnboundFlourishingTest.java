package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.Banefire;
import com.github.laxika.magicalvibes.cards.e.EnergyVortex;
import com.github.laxika.magicalvibes.cards.p.PrimordialHydra;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnboundFlourishing.class, PrimordialHydra.class, Banefire.class, EnergyVortex.class})
class UnboundFlourishingTest extends BaseCardTest {

    @Test
    void doublesXForPermanentSpellsWithoutIncreasingTheCost() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.setHand(player1, List.of(new PrimordialHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Primordial Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void copiesXInstantOrSorcery() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.setHand(player1, List.of(new Banefire()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void copiesActivatedAbilityWithXInItsCost() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        Permanent vortex = harness.addToBattlefieldAndReturn(player1, new EnergyVortex());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 2, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vortex.getCounterCount(CounterType.VORTEX)).isEqualTo(4);
    }
}
