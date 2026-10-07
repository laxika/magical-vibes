package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LotusguardDisciple;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectacularPileup.class, DuskLegionDreadnought.class, FountainOfYouth.class,
        GrizzlyBears.class, LotusguardDisciple.class, DrudgeSkeletons.class})
class SpectacularPileupTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and Vehicles, including an indestructible noncreature Vehicle")
    void destroysCreaturesAndVehicles() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new LotusguardDisciple()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, vehicle.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(vehicle.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setHand(player1, List.of(new SpectacularPileup()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Dusk Legion Dreadnought");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Cycling draws a card")
    void cycles() {
        harness.setHand(player1, List.of(new SpectacularPileup()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spectacular Pileup");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removes indestructible from creatures before destroying them")
    void destroysIndestructibleCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LotusguardDisciple());
        harness.setHand(player1, List.of(new LotusguardDisciple()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setHand(player1, List.of(new SpectacularPileup()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Lotusguard Disciple");
        harness.assertInGraveyard(player2, "Lotusguard Disciple");
        harness.assertNotOnBattlefield(player2, "Lotusguard Disciple");
    }

    @Test
    @DisplayName("Regeneration can save a creature from the destruction")
    void allowsRegeneration() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SpectacularPileup()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        harness.assertNotInGraveyard(player1, "Drudge Skeletons");
        assertThat(skeletons.isTapped()).isTrue();
        assertThat(skeletons.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Cycling discards immediately and draws only on resolution without destroying permanents")
    void cyclingPaysDiscardBeforeDrawing() {
        harness.addToBattlefield(player1, new LotusguardDisciple());
        harness.setHand(player1, List.of(new SpectacularPileup()));
        harness.setLibrary(player1, List.of(new LotusguardDisciple()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Spectacular Pileup");
        harness.assertNotInHand(player1, "Spectacular Pileup");
        harness.assertNotInHand(player1, "Lotusguard Disciple");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lotusguard Disciple");
        harness.assertOnBattlefield(player1, "Lotusguard Disciple");
    }

}
