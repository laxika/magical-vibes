package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FungalPlots;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InsidiousRoots.class, FungalPlots.class, GrizzlyBears.class})
class InsidiousRootsTest extends BaseCardTest {

    @Test
    @DisplayName("A creature card leaving the graveyard creates a Plant and puts a counter on each Plant")
    void creatureCardLeavingGraveyardCreatesAndPumpsPlant() {
        harness.addToBattlefield(player1, new InsidiousRoots());
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent plant = findPermanent(player1, "Plant");
        assertThat(plant.getCard().getSubtypes()).contains(CardSubtype.PLANT);
        assertThat(plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, plant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, plant)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature tokens can tap for one mana of any color")
    void creatureTokensCanTapForAnyColor() {
        harness.addToBattlefield(player1, new InsidiousRoots());
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent plant = findPermanent(player1, "Plant");
        plant.setSummoningSick(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(plant), null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
