package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DemonicTutor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sunimret.class, DemonicTutor.class, GrizzlyBears.class})
class SunimretTest extends BaseCardTest {

    @Test
    void exilesAllCreatures() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, firstCreature);
        harness.addToBattlefield(player2, secondCreature);
        Card sunimret = new Sunimret();

        harness.castFromHand(player1, sunimret, "{4}{B}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactlyInAnyOrder(firstCreature, secondCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sunimret);
    }

    @Test
    void offersBottomCardBeforeLibrarySearchAndResumesSearchAfterCast() {
        Card sunimret = new Sunimret();
        Card searchedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(searchedCard, sunimret));
        Card tutor = new DemonicTutor();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromHand(player1, tutor, "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(searchedCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(sunimret);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.SORCERY_SPELL
                && entry.getCard().getId().equals(sunimret.getId())
                && entry.isAlternateCost());

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sunimret);
    }

    @Test
    void decliningReverseMiracleLeavesSunimretAvailableToSearchFor() {
        Card sunimret = new Sunimret();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), sunimret));

        harness.castFromHand(player1, new DemonicTutor(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).contains(sunimret);
        harness.handleCardChosen(player1, search.params().cards().indexOf(sunimret));

        assertThat(gd.playerHands.get(player1.getId())).contains(sunimret);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(sunimret.getId()));
    }

    @Test
    void doesNotOfferReverseMiracleWhenSunimretIsNotTheBottomCard() {
        Card sunimret = new Sunimret();
        harness.setLibrary(player1, List.of(sunimret, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromHand(player1, new DemonicTutor(), "{1}{B}");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        harness.handleCardChosen(player1, search.params().cards().indexOf(sunimret));
        assertThat(gd.playerHands.get(player1.getId())).contains(sunimret);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(sunimret.getId()));
    }
}
