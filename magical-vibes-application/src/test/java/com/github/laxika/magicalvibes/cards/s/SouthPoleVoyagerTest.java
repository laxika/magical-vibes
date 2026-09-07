package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JoinTheRanks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SouthPoleVoyager.class, JoinTheRanks.class, GrizzlyBears.class})
class SouthPoleVoyagerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life for each Ally that enters and draws on the second trigger resolution")
    void gainsLifeAndDrawsOnSecondAllyEntryTriggerResolution() {
        Permanent voyager = addCreatureReady(player1, new SouthPoleVoyager());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new JoinTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(voyager).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("The Voyager's own entry triggers once without drawing")
    void ownEntryOnlyGainsLife() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new SouthPoleVoyager()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a non-Ally creature")
    void doesNotTriggerForNonAlly() {
        addCreatureReady(player1, new SouthPoleVoyager());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }
}
