package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoshimaruEverFaithful;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColonelAutumn.class, YoshimaruEverFaithful.class, GrizzlyBears.class})
class ColonelAutumnTest extends BaseCardTest {

    @Test
    @DisplayName("Its exploit trigger puts a +1/+1 counter on each creature you control")
    void exploitPutsCountersOnEachControlledCreature() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castColonel();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        Permanent colonel = findPermanent(player1, "Colonel Autumn");
        assertThat(colonel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Other legendary creatures you control gain exploit")
    void grantsExploitToOtherLegendaryCreatures() {
        harness.addToBattlefield(player1, new ColonelAutumn());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new YoshimaruEverFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        Permanent yoshimaru = findPermanent(player1, "Yoshimaru, Ever Faithful");
        assertThat(yoshimaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Colonel Autumn")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonlegendary creatures do not gain exploit")
    void doesNotGrantExploitToNonlegendaryCreatures() {
        harness.addToBattlefield(player1, new ColonelAutumn());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Colonel Autumn")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castColonel() {
        harness.setHand(player1, List.of(new ColonelAutumn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
