package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RhoxMaulers;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.cards.t.TopanFreeblade;
import com.github.laxika.magicalvibes.cards.u.UnholyHunger;
import com.github.laxika.magicalvibes.cards.w.WarOracle;
import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandrasIgnition.class, GrizzlyBears.class, HillGiant.class,
        RhoxMaulers.class, TitanicGrowth.class, TopanFreeblade.class,
        UnholyHunger.class, WarOracle.class, YokedOx.class})
class ChandrasIgnitionTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen creature deals its power to every other creature and to the opponent, but not to itself")
    void damagesEveryOtherCreatureAndOpponent() {
        // Hill Giant (3/3) is the source: it survives, both Grizzly Bears (2/2) die,
        // and the opponent takes 3.
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChandrasIgnition()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, List.of(giantId));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A creature tougher than the source's power survives")
    void toughCreatureSurvives() {
        // Grizzly Bears (2/2) as the source deals only 2 to Hill Giant (3/3).
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChandrasIgnition()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, List.of(bearsId));

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot choose a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChandrasIgnition()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void usesPowerAtResolutionAfterRespondingPumpSpell() {
        harness.addToBattlefield(player1, new RhoxMaulers());
        harness.addToBattlefield(player2, new YokedOx());
        harness.setHand(player1, List.of(new ChandrasIgnition(), new TitanicGrowth()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID sourceId = harness.getPermanentId(player1, "Rhox Maulers");

        harness.castSorcery(player1, 0, List.of(sourceId));
        harness.castAndResolveInstant(player1, 0, sourceId);
        harness.passBothPriorities();

        harness.assertLife(player2, 12);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Rhox Maulers");
        harness.assertInGraveyard(player2, "Yoked Ox");
    }

    @Test
    void dealsNoDamageWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new RhoxMaulers());
        harness.addToBattlefield(player2, new TopanFreeblade());
        harness.setHand(player1, List.of(new ChandrasIgnition()));
        harness.setHand(player2, List.of(new UnholyHunger()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.BLACK, 5);
        UUID sourceId = harness.getPermanentId(player1, "Rhox Maulers");

        harness.castSorcery(player1, 0, List.of(sourceId));
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rhox Maulers");
        harness.assertInGraveyard(player1, "Chandra's Ignition");
        harness.assertOnBattlefield(player2, "Topan Freeblade");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void creatureLifelinkGainsLifeForDamageToBothSidesAndOpponent() {
        harness.addToBattlefield(player1, new WarOracle());
        harness.addToBattlefield(player1, new TopanFreeblade());
        harness.addToBattlefield(player2, new TopanFreeblade());
        harness.setHand(player1, List.of(new ChandrasIgnition()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0,
                List.of(harness.getPermanentId(player1, "War Oracle")));

        harness.assertLife(player1, 29);
        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "War Oracle");
        harness.assertInGraveyard(player1, "Topan Freeblade");
        harness.assertInGraveyard(player2, "Topan Freeblade");
    }

    @Test
    void zeroPowerCreatureDealsNoDamage() {
        harness.addToBattlefield(player1, new YokedOx());
        harness.addToBattlefield(player2, new TopanFreeblade());
        harness.setHand(player1, List.of(new ChandrasIgnition()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0,
                List.of(harness.getPermanentId(player1, "Yoked Ox")));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Yoked Ox");
        harness.assertOnBattlefield(player2, "Topan Freeblade");
    }
}
