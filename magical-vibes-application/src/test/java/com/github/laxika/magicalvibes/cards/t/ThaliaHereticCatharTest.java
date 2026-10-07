package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SilentDeparture;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThaliaHereticCathar.class, FieldOfRuin.class, Forest.class, GrizzlyBears.class,
        TravelersAmulet.class, SilentDeparture.class})
class ThaliaHereticCatharTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creatures enter tapped")
    void opponentsCreaturesEnterTapped() {
        harness.addToBattlefield(player1, new ThaliaHereticCathar());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's creatures do NOT enter tapped")
    void controllersCreaturesDoNotEnterTapped() {
        harness.addToBattlefield(player1, new ThaliaHereticCathar());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's nonbasic lands enter tapped")
    void opponentsNonbasicLandsEnterTapped() {
        harness.addToBattlefield(player1, new ThaliaHereticCathar());
        harness.setHand(player2, List.of(new FieldOfRuin()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        Permanent land = findPermanent(player2, "Field of Ruin");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's basic lands do NOT enter tapped")
    void opponentsBasicLandsDoNotEnterTapped() {
        harness.addToBattlefield(player1, new ThaliaHereticCathar());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        Permanent forest = findPermanent(player2, "Forest");
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller's nonbasic lands enter untapped")
    void controllersNonbasicLandsEnterUntapped() {
        harness.addToBattlefield(player1, new ThaliaHereticCathar());
        harness.setHand(player1, List.of(new FieldOfRuin()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Field of Ruin").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's noncreature artifacts enter untapped")
    void opponentsNoncreatureArtifactsEnterUntapped() {
        harness.addToBattlefield(player1, new ThaliaHereticCathar());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new TravelersAmulet(), "{1}");
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Traveler's Amulet").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creatures enter untapped after Thalia leaves the battlefield")
    void effectEndsWhenThaliaLeavesBattlefield() {
        Permanent thalia = harness.addToBattlefieldAndReturn(player1, new ThaliaHereticCathar());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SilentDeparture()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player2, 0, 0, thalia.getId());
        harness.assertNotOnBattlefield(player1, "Thalia, Heretic Cathar");
        harness.assertInHand(player1, "Thalia, Heretic Cathar");
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's creatures enter tapped even without being cast")
    void creaturesEnteringWithoutBeingCastEnterTapped() {
        harness.addToBattlefield(player1, new ThaliaHereticCathar());

        Permanent opposingThalia = harness.enterBattlefieldAndReturn(player2, new ThaliaHereticCathar());

        assertThat(opposingThalia.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Thalia, Heretic Cathar").isTapped()).isFalse();
    }
}
