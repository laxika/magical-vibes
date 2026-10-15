package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.b.BloodForBones;
import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.g.GorgingVulture;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmbodimentOfAgonies.class, GreenwoodSentinel.class, Shock.class,
        Disfigure.class, Forest.class, BloodForBones.class, Murder.class, GorgingVulture.class})
class EmbodimentOfAgoniesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one counter for each different nonland mana cost")
    @CardUsed({AncestralVision.class})
    void entersWithCountersForDistinctManaCosts() {
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel(), new GreenwoodSentinel(),
                new Shock(), new Disfigure(), new Forest(), new AncestralVision()));

        harness.setHand(player1, List.of(new EmbodimentOfAgonies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent embodiment = findPermanent(player1, "Embodiment of Agonies");
        assertThat(embodiment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only the controller's graveyard")
    void countsOnlyControllersGraveyard() {
        harness.setGraveyard(player2, List.of(new GreenwoodSentinel(), new Shock()));

        harness.setHand(player1, List.of(new EmbodimentOfAgonies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Embodiment of Agonies");
    }

    @Test
    void distinguishesManaCostsWithTheSameManaValue() {
        harness.setGraveyard(player1, List.of(new Murder(), new GorgingVulture()));
        harness.setHand(player1, List.of(new EmbodimentOfAgonies()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Embodiment of Agonies")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void countsItselfWhenReturnedFromGraveyard() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setGraveyard(player1, List.of(new EmbodimentOfAgonies(), new Shock()));
        harness.setHand(player1, List.of(new BloodForBones()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(findPermanent(player1, "Embodiment of Agonies")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotCountItsOwnManaCostTwiceWhenAlreadyRepresentedInGraveyard() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setGraveyard(player1, List.of(new EmbodimentOfAgonies(), new Murder()));
        harness.setHand(player1, List.of(new BloodForBones()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(findPermanent(player1, "Embodiment of Agonies")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
