package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimeReaper.class, PathToExile.class})
class TimeReaperTest extends BaseCardTest {

    @Test
    void putsDamagedPlayersExiledCardOnBottomAndGainsLife() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        Permanent timeReaper = addCreatureReady(player1, new TimeReaper());
        timeReaper.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, exiledCard.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        List<com.github.laxika.magicalvibes.model.Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library.get(library.size() - 1)).isSameAs(exiledCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void cannotTargetACardNotOwnedByTheDamagedPlayer() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player1, List.of(exiledCard));
        Permanent timeReaper = addCreatureReady(player1, new TimeReaper());
        timeReaper.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void cannotTargetFaceDownExiledCard() {
        PathToExile exiledCard = new PathToExile();
        gd.addToExile(player2.getId(), exiledCard, null, true);
        Permanent timeReaper = addCreatureReady(player1, new TimeReaper());
        timeReaper.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotGainLifeWhenTargetLeavesExileBeforeResolution() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        Permanent timeReaper = addCreatureReady(player1, new TimeReaper());
        timeReaper.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, exiledCard.getId());
        assertThat(gd.stack).isNotEmpty();
        gd.removeFromExile(exiledCard.getId());
        gd.playerHands.get(player2.getId()).add(exiledCard);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(exiledCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(exiledCard);
        harness.assertLife(player1, 20);
    }

    @Test
    void triggerStillResolvesAfterTimeReaperLeavesBattlefield() {
        PathToExile exiledCard = new PathToExile();
        PathToExile libraryCard = new PathToExile();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setExile(player2, List.of(exiledCard));
        Permanent timeReaper = addCreatureReady(player1, new TimeReaper());
        timeReaper.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, exiledCard.getId());
        assertThat(gd.stack).isNotEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(timeReaper);
        gd.playerGraveyards.get(player1.getId()).add(timeReaper.getCard());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, exiledCard);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 16);
    }
}
