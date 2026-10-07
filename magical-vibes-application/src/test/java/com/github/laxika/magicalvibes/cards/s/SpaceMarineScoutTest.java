package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PrairieStream;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpaceMarineScout.class, Forest.class, Plains.class, PrairieStream.class})
class SpaceMarineScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Concealed Position may put a Plains onto the battlefield tapped")
    void mayPutPlainsOntoBattlefieldTapped() {
        castScout();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining Concealed Position does not search")
    void decliningSearchDoesNothing() {
        castScout();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Concealed Position does not trigger when land counts are equal")
    void noTriggerWhenLandCountsAreEqual() {
        castScout();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    private void castScout() {
        harness.castFromHand(player1, new SpaceMarineScout(), "{2}{W}");
    }

    @Test
    @DisplayName("Concealed Position checks land counts again when it resolves")
    void noSearchWhenLandCountsBecomeEqualBeforeResolution() {
        castScout();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains()));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Concealed Position can find a nonbasic Plains and still puts it in tapped")
    void findsNonbasicPlainsTappedDespiteTwoBasicLands() {
        castScout();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new PrairieStream(), new Forest()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Prairie Stream").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Concealed Position finishes when the library has no Plains")
    void searchWithNoMatchingPlains() {
        castScout();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Forest");
    }
}
