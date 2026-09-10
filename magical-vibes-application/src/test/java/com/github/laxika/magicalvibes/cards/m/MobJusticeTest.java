package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MobJustice.class, MoggFlunkies.class, VolrathsStronghold.class})
class MobJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of creatures you control")
    void dealsDamageEqualToControlledCreatureCount() {
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.setHand(player1, List.of(new MobJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Evaluates the creature count when the spell resolves")
    void evaluatesCreatureCountAtResolution() {
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.setHand(player1, List.of(new MobJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Counts creatures but not noncreature permanents or opposing creatures")
    void countsOnlyControlledCreatures() {
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.addToBattlefield(player1, new VolrathsStronghold());
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.setHand(player1, List.of(new MobJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals no damage when you control no creatures")
    void dealsNoDamageWithoutControlledCreatures() {
        harness.setHand(player1, List.of(new MobJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Can target a planeswalker")
    void canTargetPlaneswalker() {
        var chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.setHand(player1, List.of(new MobJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, chandra.getId());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }
}
