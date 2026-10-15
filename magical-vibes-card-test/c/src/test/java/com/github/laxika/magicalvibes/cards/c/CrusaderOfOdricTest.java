package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrusaderOfOdric.class, GrizzlyBears.class, GloriousAnthem.class})
class CrusaderOfOdricTest extends BaseCardTest {

    @Test
    @DisplayName("Crusader of Odric is 1/1 when it is your only creature")
    void isOneOneWhenOnlyCreature() {
        Permanent crusader = addCreatureReady(player1, new CrusaderOfOdric());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);
    }

    @Test
    @DisplayName("Crusader of Odric power and toughness equal creatures you control")
    void ptEqualsControlledCreatures() {
        Permanent crusader = addCreatureReady(player1, new CrusaderOfOdric());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(3);
    }

    @Test
    @DisplayName("Crusader of Odric counts only your creatures, not opponent creatures")
    void countsOnlyControllersCreatures() {
        Permanent crusader = addCreatureReady(player1, new CrusaderOfOdric());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);
    }

    @Test
    @DisplayName("Crusader of Odric power and toughness update as creatures enter and leave")
    void ptUpdatesAsCreaturesChange() {
        Permanent crusader = addCreatureReady(player1, new CrusaderOfOdric());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);
    }

    @Test
    @DisplayName("Crusader of Odric characteristic-defining P/T stacks with static bonuses")
    void ptStacksWithStaticBonuses() {
        Permanent crusader = addCreatureReady(player1, new CrusaderOfOdric());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(3);
    }

    @Test
    @DisplayName("Crusader in hand counts its owner's creatures without counting itself")
    void characteristicAbilityWorksInHand() {
        CrusaderOfOdric crusader = new CrusaderOfOdric();
        harness.setHand(player1, List.of(crusader));
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveCardPower(gd, crusader)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, crusader)).isZero();

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectiveCardPower(gd, crusader)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, crusader)).isEqualTo(1);
    }

    @Test
    @DisplayName("Crusader in the graveyard counts its owner's battlefield creatures")
    void characteristicAbilityWorksInGraveyard() {
        CrusaderOfOdric crusader = new CrusaderOfOdric();
        harness.setGraveyard(player1, List.of(crusader, new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveCardPower(gd, crusader)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, crusader)).isEqualTo(1);
    }

    @Test
    @DisplayName("Crusader updates its count after changing controllers")
    void countsNewControllersCreatures() {
        Permanent crusader = addCreatureReady(player1, new CrusaderOfOdric());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(crusader);
        gd.playerBattlefields.get(player2.getId()).add(crusader);

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counters modify Crusader's dynamically defined power and toughness")
    void countersApplyAfterCreatureCount() {
        Permanent crusader = addCreatureReady(player1, new CrusaderOfOdric());
        crusader.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(3);

        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(4);
    }
}
