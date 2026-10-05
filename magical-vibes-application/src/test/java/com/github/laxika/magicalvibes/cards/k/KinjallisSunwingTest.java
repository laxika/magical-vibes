package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.p.PerilousVoyage;
import com.github.laxika.magicalvibes.cards.p.PryingBlade;
import com.github.laxika.magicalvibes.cards.q.QueensCommission;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KinjallisSunwing.class, PerilousVoyage.class, PryingBlade.class, QueensCommission.class})
class KinjallisSunwingTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creatures enter tapped")
    void opponentsCreaturesEnterTapped() {
        harness.addToBattlefield(player1, new KinjallisSunwing());
        harness.setHand(player2, List.of(new KinjallisSunwing()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent sunwing = findPermanent(player2, "Kinjalli's Sunwing");
        assertThat(sunwing.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's creatures do NOT enter tapped")
    void controllersCreaturesDoNotEnterTapped() {
        harness.addToBattlefield(player1, new KinjallisSunwing());
        harness.setHand(player1, List.of(new KinjallisSunwing()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allMatch(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Opponent's creature tokens enter tapped")
    void opponentsCreatureTokensEnterTapped() {
        harness.addToBattlefield(player1, new KinjallisSunwing());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new QueensCommission()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Controller's creature tokens enter untapped")
    void controllersCreatureTokensEnterUntapped() {
        harness.addToBattlefield(player1, new KinjallisSunwing());
        harness.setHand(player1, List.of(new QueensCommission()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(3)
                .allMatch(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Opponent's noncreature artifacts enter untapped")
    void opponentsNoncreatureArtifactsEnterUntapped() {
        harness.addToBattlefield(player1, new KinjallisSunwing());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new PryingBlade()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Prying Blade").isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Sunwing still makes opposing creatures enter tapped")
    void tappedSunwingStillApplies() {
        harness.addToBattlefieldAndReturn(player1, new KinjallisSunwing()).tap();

        Permanent entering = harness.enterBattlefieldAndReturn(player2, new KinjallisSunwing());

        assertThat(entering.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing Sunwing before a creature resolves stops its entry restriction")
    void removingSunwingBeforeResolutionStopsRestriction() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KinjallisSunwing());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new KinjallisSunwing(), new PerilousVoyage()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player2, 0);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.assertNotOnBattlefield(player1, "Kinjalli's Sunwing");
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Kinjalli's Sunwing").isTapped()).isFalse();
    }
}
