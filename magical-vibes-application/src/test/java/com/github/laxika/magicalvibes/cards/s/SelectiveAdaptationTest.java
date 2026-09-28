package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelectiveAdaptation.class, Forest.class, SerraAngel.class, VampireNighthawk.class})
class SelectiveAdaptationTest extends BaseCardTest {

    @Test
    void assignsMultiKeywordCardsToChosenKeywordsThenChoosesBattlefieldCard() {
        Card serraAngel = new SerraAngel();
        Card vampireNighthawk = new VampireNighthawk();
        List<Card> library = List.of(serraAngel, vampireNighthawk,
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        castAndResolve(library);

        PendingInteraction.LibrarySearch flyingPick = activeLibrarySearch();
        assertThat(flyingPick.params().cards()).containsExactly(serraAngel, vampireNighthawk);
        chooseLibraryCard(flyingPick, serraAngel);

        PendingInteraction.LibrarySearch deathtouchPick = activeLibrarySearch();
        assertThat(deathtouchPick.params().cards()).containsExactly(vampireNighthawk);
        chooseLibraryCard(deathtouchPick, vampireNighthawk);

        PendingInteraction.LibrarySearch battlefieldPick = activeLibrarySearch();
        assertThat(battlefieldPick.params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_ONE_AND_PUT_REST_INTO_HAND);
        assertThat(battlefieldPick.params().cards()).containsExactly(serraAngel, vampireNighthawk);
        chooseLibraryCard(battlefieldPick, vampireNighthawk);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactly(vampireNighthawk);
        harness.assertInHand(player1, "Serra Angel");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Selective Adaptation", "Forest", "Forest", "Forest", "Forest", "Forest");
    }

    @Test
    void putsAllRevealedCardsIntoGraveyardWhenNoListedKeywordIsFound() {
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
        castAndResolve(library);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Selective Adaptation", "Forest", "Forest", "Forest", "Forest", "Forest", "Forest", "Forest");
    }

    private void castAndResolve(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SelectiveAdaptation()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch activeLibrarySearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void chooseLibraryCard(PendingInteraction.LibrarySearch search, Card card) {
        int index = search.params().cards().indexOf(card);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(index));
    }
}
