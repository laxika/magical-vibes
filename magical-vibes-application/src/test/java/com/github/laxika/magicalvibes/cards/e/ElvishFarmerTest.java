package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.f.FarrelitePriest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishFarmer.class, FarrelitePriest.class, AmoeboidChangeling.class})
class ElvishFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent farmer = addFarmer();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(farmer.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep trigger only fires during its controller's upkeep")
    void upkeepTriggerOnlyFiresDuringControllerUpkeep() {
        Permanent farmer = addFarmer();

        advanceToUpkeep(player2);

        assertThat(farmer.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling")
    void removesThreeSporeCountersAndCreatesSaproling() {
        Permanent farmer = addFarmer();
        farmer.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(farmer.getCounterCount(CounterType.FUNGUS)).isZero();
        Permanent saproling = findPermanents(player1, "Saproling").getFirst();
        assertThat(saproling.getCard().getPower()).isEqualTo(1);
        assertThat(saproling.getCard().getToughness()).isEqualTo(1);
        assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(saproling.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
    }

    @Test
    @DisplayName("The token ability removes exactly three spore counters")
    void tokenAbilityRemovesExactlyThreeSporeCounters() {
        Permanent farmer = addFarmer();
        farmer.setCounterCount(CounterType.FUNGUS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(farmer.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Saproling gains two life")
    void sacrificingSaprolingGainsTwoLife() {
        Permanent farmer = addFarmer();
        farmer.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("The life-gain ability cannot sacrifice a non-Saproling creature")
    void lifeGainAbilityRequiresSaproling() {
        addFarmer();
        harness.addToBattlefield(player1, new FarrelitePriest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The life-gain ability can sacrifice the source when it is a Saproling")
    void lifeGainAbilityCanSacrificeSourceWhenItIsASaproling() {
        Permanent amoeboid = addCreatureReady(player1, new AmoeboidChangeling());
        Permanent farmer = addFarmer();

        harness.activateAbility(player1, 0, 0, null, farmer.getId());
        harness.passBothPriorities();

        amoeboid.untap();
        harness.activateAbility(player1, 0, 1, null, amoeboid.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, farmer, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.hasKeyword(gd, amoeboid, Keyword.CHANGELING)).isFalse();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(findPermanents(player1, "Elvish Farmer")).isEmpty();
    }

    @Test
    @DisplayName("The token ability requires three spore counters")
    void tokenAbilityRequiresThreeSporeCounters() {
        Permanent farmer = addFarmer();
        farmer.setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addFarmer() {
        return addCreatureReady(player1, new ElvishFarmer());
    }
}
