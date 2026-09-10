package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.IronMaiden;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianBroodlings.class, YavimayaWurm.class, IronMaiden.class})
class PhyrexianBroodlingsTest extends BaseCardTest {

    @Test
    void sacrificingACreaturePutsACounterOnPhyrexianBroodlings() {
        Permanent broodlings = addCreatureReady(player1, new PhyrexianBroodlings());
        Permanent sacrifice = addCreatureReady(player1, new YavimayaWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(broodlings.getId(), sacrifice.getId());

        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(broodlings.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Yavimaya Wurm");
        harness.assertInGraveyard(player1, "Yavimaya Wurm");
    }

    @Test
    void onlyCreaturesCanBeSacrificed() {
        Permanent broodlings = addCreatureReady(player1, new PhyrexianBroodlings());
        Permanent creature = addCreatureReady(player1, new YavimayaWurm());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IronMaiden());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(broodlings.getId(), creature.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(artifact.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(broodlings.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Yavimaya Wurm");
        harness.assertOnBattlefield(player1, "Iron Maiden");
    }

    @Test
    void sacrificingPhyrexianBroodlingsItselfDoesNotPutCounterOnIt() {
        Permanent broodlings = addCreatureReady(player1, new PhyrexianBroodlings());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Broodlings");
        harness.assertInGraveyard(player1, "Phyrexian Broodlings");
        assertThat(broodlings.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
