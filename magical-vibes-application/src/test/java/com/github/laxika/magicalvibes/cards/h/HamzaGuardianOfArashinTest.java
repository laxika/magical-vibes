package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HamzaGuardianOfArashin.class, Forest.class, GrizzlyBears.class})
class HamzaGuardianOfArashinTest extends BaseCardTest {

    @Test
    @DisplayName("The spell costs one less for each creature you control with a +1/+1 counter")
    void reducesOwnCostForCounterBearingCreaturesYouControl() {
        addCounterBearingCreature(player1);
        addCounterBearingCreature(player1);
        harness.setHand(player1, List.of(new HamzaGuardianOfArashin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Creature spells you cast cost one less for each matching creature you control")
    void reducesCreatureSpellCosts() {
        harness.addToBattlefield(player1, new HamzaGuardianOfArashin());
        addCounterBearingCreature(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Only your creatures with +1/+1 counters count toward the reductions")
    void doesNotCountNoncreaturesOrOpponentsCreatures() {
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        ownForest.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCounterBearingCreature(player2);
        harness.setHand(player1, List.of(new HamzaGuardianOfArashin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCounterBearingCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return creature;
    }
}
