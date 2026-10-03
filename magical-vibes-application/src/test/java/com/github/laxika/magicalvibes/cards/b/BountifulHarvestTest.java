package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BountifulHarvest.class, Forest.class, Island.class, RuneclawBear.class})
class BountifulHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts sorcery spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BountifulHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BountifulHarvest.class);
    }

    @Test
    @DisplayName("Gains 1 life for each land you control")
    void gainsLifePerControlledLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BountifulHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Does not count opponent's lands")
    void doesNotCountOpponentLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BountifulHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not count non-land permanents")
    void doesNotCountNonLandPermanents() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new RuneclawBear());

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BountifulHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Gains life from own lands but not opponent's lands or own creatures")
    void gainsLifeOnlyFromOwnLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new BountifulHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Gains no life when controlling no lands")
    void gainsNoLifeWithNoLands() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BountifulHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts lands at resolution, including tapped lands")
    void countsCurrentLandsAtResolution() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BountifulHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new Island());
        findPermanent(player1, "Island").setTapped(true);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
