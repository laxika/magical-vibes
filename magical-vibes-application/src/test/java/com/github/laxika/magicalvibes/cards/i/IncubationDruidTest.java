package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AncientTomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NykthosShrineToNyx;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncubationDruid.class, Forest.class, Island.class, AncientTomb.class,
        NykthosShrineToNyx.class, SongOfTheDryads.class})
class IncubationDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one mana of a type a land you control could produce")
    void addsOneManaWithoutCounter() {
        addDruid();
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds three mana of the chosen type when Incubation Druid has a +1/+1 counter")
    void addsThreeManaOfOneChosenTypeWithCounter() {
        Permanent druid = addDruid();
        druid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ManaColorChoice.class);
        assertThat(((ChoiceContext.ManaColorChoice) choice.context()).amount())
                .isEqualTo(3);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Any type includes colorless mana")
    void addsColorlessManaFromColorlessLand() {
        Permanent druid = addDruid();
        druid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new AncientTomb());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adapt 3 puts three +1/+1 counters on Incubation Druid")
    void adaptsThree() {
        addDruid();
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent druid = findPermanent(player1, "Incubation Druid");
        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adapt can be activated once Incubation Druid has a +1/+1 counter")
    void adaptsWithCounter() {
        Permanent druid = addDruid();
        druid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void tappedLandStillDeterminesManaType() {
        Permanent druid = addDruid();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void opponentsLandsDoNotProvideManaTypes() {
        addDruid();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void noLandsProducesNoMana() {
        Permanent druid = addDruid();

        harness.activateAbility(player1, 0, null, null);

        assertThat(druid.isTapped()).isTrue();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void multipleCountersStillProduceOnlyThreeMana() {
        Permanent druid = addDruid();
        druid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void adaptChecksCountersAtResolution() {
        Permanent druid = addDruid();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        druid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveAllTriggers();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adaptWorksIfLastCounterIsRemovedBeforeResolution() {
        Permanent druid = addDruid();
        druid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        druid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        resolveAllTriggers();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void landCreatedBySongOfTheDryadsProvidesGreenMana() {
        addDruid();
        Permanent enchantedDruid = harness.addToBattlefieldAndReturn(player1, new IncubationDruid());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SongOfTheDryads());
        aura.setAttachedTo(enchantedDruid.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void nykthosProvidesGreenWithPositiveGreenDevotion() {
        addDruid();
        harness.addToBattlefield(player1, new NykthosShrineToNyx());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addDruid() {
        return addCreatureReady(player1, new IncubationDruid());
    }
}
