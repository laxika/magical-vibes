package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DayOfDestiny;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.s.Shuko;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrbOfDreams.class, GnarledMass.class, TendoIceBridge.class, DayOfDestiny.class, Shuko.class})
class OrbOfDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures enter tapped for both players")
    void creaturesEnterTappedForBothPlayers() {
        harness.addToBattlefield(player1, new OrbOfDreams());

        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        harness.passBothPriorities();

        Permanent ownCreature = findPermanent(player1, "Gnarled Mass");
        assertThat(ownCreature.isTapped()).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GnarledMass(), "{1}{G}{G}");
        harness.passBothPriorities();

        Permanent opponentCreature = findPermanent(player2, "Gnarled Mass");
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lands enter tapped")
    void landsEnterTapped() {
        harness.addToBattlefield(player1, new OrbOfDreams());
        harness.setHand(player1, List.of(new TendoIceBridge()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Tendo Ice Bridge").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchantments enter tapped")
    void enchantmentsEnterTapped() {
        harness.addToBattlefield(player1, new OrbOfDreams());

        harness.castFromHand(player1, new DayOfDestiny(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Day of Destiny").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Artifacts enter tapped")
    void artifactsEnterTapped() {
        harness.addToBattlefield(player1, new OrbOfDreams());

        harness.castFromHand(player1, new Shuko(), "{1}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Shuko").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Existing permanents are not tapped when Orb of Dreams enters")
    void existingPermanentsAreNotTappedWhenOrbEnters() {
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.addToBattlefield(player1, new OrbOfDreams());

        assertThat(findPermanent(player1, "Tendo Ice Bridge").isTapped()).isFalse();
    }
}
