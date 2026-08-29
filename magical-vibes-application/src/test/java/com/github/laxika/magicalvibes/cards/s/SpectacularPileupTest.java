package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LotusguardDisciple;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
}
