package com.github.laxika.magicalvibes.cards.a;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({AudaciousReshapers.class, SolRing.class})
class AudaciousReshapersTest extends BaseCardTest {

    @Test
    void putsTheFirstArtifactOntoTheBattlefieldAndDealsDamageForAllRevealedCards() {
        Permanent reshapers = addCreatureReady(player1, new AudaciousReshapers());
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Card foundArtifact = new SolRing();
        Card interveningCard = new AudaciousReshapers();
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
        Card firstCard = new AudaciousReshapers();
        Card secondCard = new AudaciousReshapers();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstCard, secondCard);
    }

    @Test
    void stopsAtTheFirstArtifactAndLeavesUnrevealedCardsAboveTheReturnedCards() {
        addCreatureReady(player1, new AudaciousReshapers());
        harness.addToBattlefield(player1, new SolRing());
        Card firstRevealed = new AudaciousReshapers();
        Card secondRevealed = new AudaciousReshapers();
        Card foundArtifact = new SolRing();
        Card unrevealedArtifact = new SolRing();
        harness.setLibrary(player1, List.of(firstRevealed, secondRevealed, foundArtifact, unrevealedArtifact));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unrevealedArtifact);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(firstRevealed, secondRevealed);
        assertThat(findPermanent(player1, "Sol Ring").getCard()).isSameAs(foundArtifact);
    }

    @Test
    void dealsOneDamageWhenTheTopCardIsAnArtifact() {
        addCreatureReady(player1, new AudaciousReshapers());
        harness.addToBattlefield(player1, new SolRing());
        Card foundArtifact = new SolRing();
        harness.setLibrary(player1, List.of(foundArtifact));

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Sol Ring");
        harness.assertNotOnBattlefield(player1, "Sol Ring");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(findPermanent(player1, "Sol Ring").getCard()).isSameAs(foundArtifact);
        assertThat(findPermanent(player1, "Sol Ring").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryStillRequiresTheSacrificeButDealsNoDamage() {
        Permanent reshapers = addCreatureReady(player1, new AudaciousReshapers());
        harness.addToBattlefield(player1, new SolRing());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(reshapers.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Sol Ring");
        harness.assertNotOnBattlefield(player1, "Sol Ring");
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotPayTheCostWithAnOpponentsArtifactOrANonartifactCreature() {
        addCreatureReady(player1, new AudaciousReshapers());
        harness.addToBattlefield(player2, new SolRing());
        harness.setLibrary(player1, List.of(new SolRing()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Sol Ring");
        harness.assertOnBattlefield(player1, "Audacious Reshapers");
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent reshapers = addCreatureReady(player1, new AudaciousReshapers());
        reshapers.tap();
        harness.addToBattlefield(player1, new SolRing());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sol Ring");
        harness.assertNotInGraveyard(player1, "Sol Ring");
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new AudaciousReshapers());
        harness.addToBattlefield(player1, new SolRing());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sol Ring");
        harness.assertNotInGraveyard(player1, "Sol Ring");
    }
}
