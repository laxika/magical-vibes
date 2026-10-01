package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianHarvester.class, Forest.class, GrizzlyBears.class, Shock.class})
class PhyrexianHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Seeks nonland cards equal to damage and discards those exact cards at the next end step")
    void seeksAndDiscardsThoseExactCards() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new PhyrexianHarvester());
        Card extra = new Forest();
        Card soughtCreature = new GrizzlyBears();
        Card soughtSpell = new Shock();
        Card land = new Forest();
        harness.setHand(player1, List.of(extra));
        harness.setLibrary(player1, List.of(soughtCreature, land, soughtSpell));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harvester.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(extra, soughtCreature, soughtSpell);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(extra);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(soughtCreature.getId(), soughtSpell.getId());
    }

    @Test
    @DisplayName("Does not seek when the library has no nonland cards")
    void doesNotSeekLands() {
        harness.setHand(player1, List.of());
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new PhyrexianHarvester());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harvester.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
