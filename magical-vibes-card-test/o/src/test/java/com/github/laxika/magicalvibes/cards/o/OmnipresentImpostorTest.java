package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.Cultivate;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmnipresentImpostor.class, SakuraTribeElder.class, Forest.class,
        EchoingTruth.class, Cultivate.class})
class OmnipresentImpostorTest extends BaseCardTest {

    @Test
    void mayBeChosenInPlaceOfARestrictedBasicLandSearch() {
        harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder());
        Card impostor = new OmnipresentImpostor();
        harness.setLibrary(player1, List.of(impostor, new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).contains(impostor);

        harness.handleCardChosen(player1, search.params().cards().indexOf(impostor));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(impostor.getId()) && permanent.isTapped());
    }

    @Test
    void sharesTheNameOfAnotherTargetedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.addToBattlefield(player2, new OmnipresentImpostor());
        harness.setHand(player1, List.of(new EchoingTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Sakura-Tribe Elder");
        harness.assertInHand(player2, "Omnipresent Impostor");
        harness.assertNotOnBattlefield(player2, "Omnipresent Impostor");
    }

    @Test
    void targetingImpostorAlsoReturnsOtherNamedPermanentsIncludingLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OmnipresentImpostor());
        harness.addToBattlefield(player2, new SakuraTribeElder());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new EchoingTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Omnipresent Impostor");
        harness.assertInHand(player2, "Sakura-Tribe Elder");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void mayReplaceBothCardsInATwoBasicLandSearch() {
        Card first = new OmnipresentImpostor();
        Card second = new OmnipresentImpostor();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Cultivate()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).contains(first, second);
        harness.handleCardChosen(player1, search.params().cards().indexOf(first));

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).contains(second);
        harness.handleCardChosen(player1, search.params().cards().indexOf(second));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()) && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
