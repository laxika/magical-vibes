package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenesisUltimatum.class, Forest.class, GrizzlyBears.class, HolyDay.class, MindStone.class, Shock.class, Pacifism.class})
class GenesisUltimatumTest extends BaseCardTest {

    @Test
    void putsChosenPermanentsOntoBattlefieldAndRestIntoHand() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card mindStone = new MindStone();
        Card holyDay = new HolyDay();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(bears, holyDay, forest, shock, mindStone));
        Card ultimatum = new GenesisUltimatum();
        cast(ultimatum);

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), forest.getId(), mindStone.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), forest.getId(), mindStone.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(bears.getId(), forest.getId(), mindStone.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(holyDay.getId(), shock.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultimatum);
    }

    @Test
    void mayChooseNoPermanentsAndPutAllFiveCardsIntoHand() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card holyDay = new HolyDay();
        Card mindStone = new MindStone();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(bears, forest, holyDay, mindStone, shock));
        Card ultimatum = new GenesisUltimatum();
        cast(ultimatum);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(bears.getId(), forest.getId(), mindStone.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(bears.getId(), forest.getId(), holyDay.getId(), mindStone.getId(), shock.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultimatum);
    }

    @Test
    void onlyLooksAtFiveCardsAndMaySelectASubsetOfPermanents() {
        Card chosen = new Forest();
        Card declined = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        Card fifth = new Forest();
        Card sixth = new Forest();
        harness.setLibrary(player1, List.of(chosen, declined, third, fourth, fifth, sixth));
        Card ultimatum = new GenesisUltimatum();
        cast(ultimatum);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getId())
                .containsExactly(chosen.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(declined.getId(), third.getId(), fourth.getId(), fifth.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultimatum);
    }

    @Test
    void putsNonpermanentsIntoHandWithoutASelection() {
        Card first = new Shock();
        Card second = new HolyDay();
        harness.setLibrary(player1, List.of(first, second));
        Card ultimatum = new GenesisUltimatum();
        cast(ultimatum);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultimatum);
    }

    @Test
    void resolvesWithAnEmptyLibraryWithoutDrawingOrLosing() {
        harness.setLibrary(player1, List.of());
        Card ultimatum = new GenesisUltimatum();
        cast(ultimatum);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultimatum);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ultimatum);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void auraSelectedAlongsideALandEnchantsAnExistingCreature() {
        var host = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card aura = new Pacifism();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(aura, land));
        Card ultimatum = new GenesisUltimatum();
        cast(ultimatum);

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId(), land.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent -> {
            assertThat(permanent.getCard()).isSameAs(aura);
            assertThat(permanent.getAttachedTo()).isEqualTo(host.getId());
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard())
                .contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultimatum);
    }

    @Test
    void auraCannotEnchantACreatureEnteringAlongsideIt() {
        Card aura = new Pacifism();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(aura, creature));
        Card ultimatum = new GenesisUltimatum();
        cast(ultimatum);

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId(), creature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard())
                .containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultimatum);
    }

    @Test
    void selectedAuraWithNoLegalHostRemainsInLibrary() {
        Card aura = new Pacifism();
        harness.setLibrary(player1, List.of(aura));
        Card ultimatum = new GenesisUltimatum();
        cast(ultimatum);

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultimatum);
    }

    private void cast(Card ultimatum) {
        harness.castFromHand(player1, ultimatum, "{G}{G}{U}{U}{U}{R}{R}");
        harness.passBothPriorities();
    }
}
