package com.github.laxika.magicalvibes.cards.a;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({AudaciousReshapers.class, SolRing.class, GrizzlyBears.class})
class AudaciousReshapersTest extends BaseCardTest {

    @Test
    void putsTheFirstArtifactOntoTheBattlefieldAndDealsDamageForAllRevealedCards() {
        Permanent reshapers = addCreatureReady(player1, new AudaciousReshapers());
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Card foundArtifact = new SolRing();
        Card interveningCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(interveningCard, foundArtifact));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(reshapers.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Sol Ring");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(foundArtifact.getId())
                .doesNotContain(sacrificedArtifact.getCard().getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(interveningCard);
    }

    @Test
    void dealsDamageForTheWholeLibraryWhenNoArtifactIsFound() {
        addCreatureReady(player1, new AudaciousReshapers());
        harness.addToBattlefieldAndReturn(player1, new SolRing());
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstCard, secondCard);
    }
}
