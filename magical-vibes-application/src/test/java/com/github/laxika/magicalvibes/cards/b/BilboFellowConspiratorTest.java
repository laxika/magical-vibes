package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.ForswornPaladin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WitchsOven;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BilboFellowConspirator.class, BristlebudFarmer.class, ForswornPaladin.class,
        GrizzlyBears.class, WitchsOven.class})
class BilboFellowConspiratorTest extends BaseCardTest {

    @Test
    void addsTreasureForEachFoodTokenCreated() {
        harness.addToBattlefield(player1, new BilboFellowConspirator());
        harness.addToBattlefield(player1, new WitchsOven());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void addsTreasureForEachFoodInOneCreationEvent() {
        harness.addToBattlefield(player1, new BilboFellowConspirator());
        harness.setHand(player1, List.of(new BristlebudFarmer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void doesNotAddTreasureToTreasureCreation() {
        harness.addToBattlefield(player1, new BilboFellowConspirator());
        addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(countPermanents(player1, "Treasure")).isOne();
    }
}
