package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CausticBronco.class, GrizzlyBears.class, HillGiant.class})
class CausticBroncoTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking unsaddled reveals the top card, puts it into hand, and loses its mana value in life")
    void attacksUnsaddled() {
        Card topCard = new GrizzlyBears();
        Permanent bronco = addCreatureReady(player1, new CausticBronco());
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bronco.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Attacking saddled makes each opponent lose the revealed card's mana value in life")
    void attacksSaddled() {
        Card topCard = new GrizzlyBears();
        Permanent bronco = addCreatureReady(player1, new CausticBronco());
        Permanent saddler = addCreatureReady(player1, new HillGiant());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bronco.isSaddled()).isTrue();
        assertThat(saddler.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
