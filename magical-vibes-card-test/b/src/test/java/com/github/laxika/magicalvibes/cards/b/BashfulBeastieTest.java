package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.SlaveringBranchsnapper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BashfulBeastie.class, Murder.class, SlaveringBranchsnapper.class})
class BashfulBeastieTest extends BaseCardTest {

    @Test
    void diesAndManifestsDread() {
        Permanent beastie = harness.addToBattlefieldAndReturn(player1, new BashfulBeastie());
        Card manifestedCard = new SlaveringBranchsnapper();
        Card graveyardCard = new SlaveringBranchsnapper();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, beastie.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void manifestsTheOnlyLibraryCardAndCanTurnItFaceUpForItsManaCost() {
        Permanent beastie = harness.addToBattlefieldAndReturn(player1, new BashfulBeastie());
        Card manifestedCard = new SlaveringBranchsnapper();
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, beastie.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2).contains(beastie.getCard()).doesNotContain(manifestedCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.getCard().getId()).isEqualTo(manifestedCard.getId());
    }

    @Test
    void canChooseTheSecondCardAndManifestANoncreature() {
        Permanent beastie = harness.addToBattlefieldAndReturn(player1, new BashfulBeastie());
        Card graveyardCard = new SlaveringBranchsnapper();
        Card manifestedCard = new Murder();
        harness.setLibrary(player1, List.of(graveyardCard, manifestedCard));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, beastie.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.isManifested()).isTrue();
            assertThat(permanent.isFaceDown()).isTrue();
            assertThat(permanent.getCard().getId()).isEqualTo(manifestedCard.getId());
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard).doesNotContain(manifestedCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentControlledBeastieManifestsFromItsControllersLibrary() {
        Permanent beastie = harness.addToBattlefieldAndReturn(player2, new BashfulBeastie());
        Card manifestedCard = new SlaveringBranchsnapper();
        Card graveyardCard = new Murder();
        Card untouchedCard = new BashfulBeastie();
        harness.setLibrary(player2, List.of(manifestedCard, graveyardCard));
        harness.setLibrary(player1, List.of(untouchedCard));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, beastie.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.isManifested()).isTrue();
            assertThat(permanent.getCard().getId()).isEqualTo(manifestedCard.getId());
        });
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(beastie.getCard(), graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouchedCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotCreateACreatureOrRequireAChoice() {
        Permanent beastie = harness.addToBattlefieldAndReturn(player1, new BashfulBeastie());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, beastie.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Bashful Beastie");
    }
}
