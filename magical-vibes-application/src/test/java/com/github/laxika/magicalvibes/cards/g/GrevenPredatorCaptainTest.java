package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrevenPredatorCaptain.class, HillGiant.class, GrizzlyBears.class})
class GrevenPredatorCaptainTest extends BaseCardTest {

    @Test
    void getsPowerForLifeLostThisTurn() {
        Permanent greven = addCreatureReady(player1, new GrevenPredatorCaptain());

        assertThat(gqs.getEffectivePower(gd, greven)).isEqualTo(5);
        gd.lifeLostThisTurn.put(player1.getId(), 4);

        assertThat(gqs.getEffectivePower(gd, greven)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, greven)).isEqualTo(5);
    }

    @Test
    void attackingMaySacrificeAnotherCreatureToDrawAndLoseLife() {
        Permanent greven = addCreatureReady(player1, new GrevenPredatorCaptain());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        gd.lifeLostThisTurn.put(player1.getId(), 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, hillGiant.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .contains("Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gqs.getEffectivePower(gd, greven)).isEqualTo(10);
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void mayDeclineToSacrificeAnotherCreature() {
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new GrevenPredatorCaptain());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
