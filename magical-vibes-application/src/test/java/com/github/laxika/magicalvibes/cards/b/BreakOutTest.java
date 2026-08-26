package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreakOut.class, CrawWurm.class, GrizzlyBears.class, Shock.class})
class BreakOutTest extends BaseCardTest {

    @Test
    void putsASelectedLowManaValueCreatureOntoTheBattlefieldWithHaste() {
        Card bears = new GrizzlyBears();
        List<Card> library = libraryWith(bears);
        castBreakOut(library);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        Permanent permanent = findPermanentByCardId(bears.getId());
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(idsExcept(library, bears.getId()));
    }

    @Test
    void decliningTheBattlefieldChoiceLeavesTheCreatureInHand() {
        Card bears = new GrizzlyBears();
        List<Card> library = libraryWith(bears);
        castBreakOut(library);

        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(idsExcept(library, bears.getId()));
    }

    @Test
    void putsASelectedHighManaValueCreatureIntoHand() {
        Card wurm = new CrawWurm();
        List<Card> library = libraryWith(wurm);
        castBreakOut(library);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(wurm.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(idsExcept(library, wurm.getId()));
    }

    @Test
    void decliningTheCreatureRevealReturnsAllLookedAtCardsToTheLibrary() {
        List<Card> library = libraryWith(new GrizzlyBears());
        castBreakOut(library);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(library.stream().map(Card::getId).toArray(UUID[]::new));
    }

    private void castBreakOut(List<Card> library) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BreakOut()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    private List<Card> libraryWith(Card creature) {
        return List.of(creature, new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }

    private UUID[] idsExcept(List<Card> cards, UUID excludedId) {
        return cards.stream()
                .filter(card -> !card.getId().equals(excludedId))
                .map(Card::getId)
                .toArray(UUID[]::new);
    }
}
