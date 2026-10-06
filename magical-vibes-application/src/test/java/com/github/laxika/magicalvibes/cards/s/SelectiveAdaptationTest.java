package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AkromaAngelOfWrath;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinDarkDwellers;
import com.github.laxika.magicalvibes.cards.n.NyxWeaver;
import com.github.laxika.magicalvibes.cards.z.ZetalpaPrimalDawn;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelectiveAdaptation.class, Forest.class, SerraAngel.class, VampireNighthawk.class,
        AkromaAngelOfWrath.class, ZetalpaPrimalDawn.class, SlipperyBogbonder.class,
        GoblinDarkDwellers.class, NyxWeaver.class})
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

    @Test
    void canChooseNighthawkForFlyingAndAngelForVigilance() {
        Card nighthawk = new VampireNighthawk();
        Card angel = new SerraAngel();
        castAndResolve(List.of(nighthawk, angel));

        chooseLibraryCard(activeLibrarySearch(), nighthawk);
        assertThat(activeLibrarySearch().params().cards()).containsExactly(angel);
        chooseLibraryCard(activeLibrarySearch(), angel);
        chooseLibraryCard(activeLibrarySearch(), angel);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(angel);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nighthawk);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Selective Adaptation");
        assertThat(activeLibrarySearch()).isNull();
    }

    @Test
    void choosesDistinctCopiesForFlyingDeathtouchAndLifelink() {
        Card flying = new VampireNighthawk();
        Card deathtouch = new VampireNighthawk();
        Card lifelink = new VampireNighthawk();
        Card unchosen = new VampireNighthawk();
        castAndResolve(List.of(flying, deathtouch, lifelink, unchosen));

        chooseLibraryCard(activeLibrarySearch(), flying);
        assertThat(activeLibrarySearch().params().cards()).containsExactly(deathtouch, lifelink, unchosen);
        chooseLibraryCard(activeLibrarySearch(), deathtouch);
        assertThat(activeLibrarySearch().params().cards()).containsExactly(lifelink, unchosen);
        chooseLibraryCard(activeLibrarySearch(), lifelink);
        assertThat(activeLibrarySearch().params().cards()).containsExactly(flying, deathtouch, lifelink);
        chooseLibraryCard(activeLibrarySearch(), lifelink);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(lifelink);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(flying, deathtouch);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unchosen)
                .hasSize(2);
        assertThat(activeLibrarySearch()).isNull();
    }

    @Test
    void putsOnlyChosenCardOntoBattlefieldFromShortLibrary() {
        Card nighthawk = new VampireNighthawk();
        Card forest = new Forest();
        castAndResolve(List.of(nighthawk, forest));

        chooseLibraryCard(activeLibrarySearch(), nighthawk);
        assertThat(activeLibrarySearch().params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_ONE_AND_PUT_REST_INTO_HAND);
        chooseLibraryCard(activeLibrarySearch(), nighthawk);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(nighthawk);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(activeLibrarySearch()).isNull();
    }

    @Test
    void doesNotRevealOrChooseTheEighthCard() {
        Card eighth = new VampireNighthawk();
        castAndResolve(List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), eighth));

        assertThat(activeLibrarySearch()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eighth);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        castAndResolve(List.of());

        assertThat(activeLibrarySearch()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Selective Adaptation");
    }

    @Test
    void selectsCopiesForFlyingDoubleStrikeIndestructibleTrampleAndVigilance() {
        assertAllCopiesSelected(List.of(new ZetalpaPrimalDawn(), new ZetalpaPrimalDawn(),
                new ZetalpaPrimalDawn(), new ZetalpaPrimalDawn(), new ZetalpaPrimalDawn()));
    }

    @Test
    void selectsCopiesForFlyingFirstStrikeHasteTrampleAndVigilance() {
        assertAllCopiesSelected(List.of(new AkromaAngelOfWrath(), new AkromaAngelOfWrath(),
                new AkromaAngelOfWrath(), new AkromaAngelOfWrath(), new AkromaAngelOfWrath()));
    }

    @Test
    void selectsHexproofMenaceAndReachCards() {
        Card hexproof = new SlipperyBogbonder();
        Card menace = new GoblinDarkDwellers();
        Card reach = new NyxWeaver();
        castAndResolve(List.of(hexproof, menace, reach));

        assertThat(activeLibrarySearch().params().cards()).containsExactly(hexproof);
        chooseLibraryCard(activeLibrarySearch(), hexproof);
        assertThat(activeLibrarySearch().params().cards()).containsExactly(menace);
        chooseLibraryCard(activeLibrarySearch(), menace);
        assertThat(activeLibrarySearch().params().cards()).containsExactly(reach);
        chooseLibraryCard(activeLibrarySearch(), reach);
        chooseLibraryCard(activeLibrarySearch(), reach);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(reach);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(hexproof, menace);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Selective Adaptation");
        assertThat(activeLibrarySearch()).isNull();
    }

    private void assertAllCopiesSelected(List<Card> copies) {
        castAndResolve(copies);
        for (Card copy : copies) {
            chooseLibraryCard(activeLibrarySearch(), copy);
        }
        assertThat(activeLibrarySearch().params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_ONE_AND_PUT_REST_INTO_HAND);
        assertThat(activeLibrarySearch().params().cards()).containsExactlyElementsOf(copies);
        chooseLibraryCard(activeLibrarySearch(), copies.getFirst());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(copies.getFirst());
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyElementsOf(copies.subList(1, copies.size()));
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Selective Adaptation");
        assertThat(activeLibrarySearch()).isNull();
    }

    private void castAndResolve(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new SelectiveAdaptation(), "{4}{G}{G}");
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch activeLibrarySearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void chooseLibraryCard(PendingInteraction.LibrarySearch search, Card card) {
        int index = search.params().cards().indexOf(card);
        harness.handleCardChosen(player1, index);
    }
}
