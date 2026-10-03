package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaruCavalier.class, GlorySeeker.class})
class DaruCavalierTest extends BaseCardTest {

    @Test
    void acceptingMaySearchesForOneDaruCavalier() {
        castDaruCavalier();
        harness.setLibrary(player1, List.of(new DaruCavalier(), new GlorySeeker()));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName).containsExactly("Daru Cavalier");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Daru Cavalier");
    }

    @Test
    void decliningMayDoesNotSearch() {
        castDaruCavalier();
        harness.setLibrary(player1, List.of(new DaruCavalier(), new GlorySeeker()));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .doesNotContain("Daru Cavalier");
    }

    @Test
    void noMatchingCardDoesNotCreateSearchPrompt() {
        castDaruCavalier();
        harness.setLibrary(player1, List.of(new GlorySeeker()));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotInHand(player1, "Daru Cavalier");
    }

    @Test
    void firstStrikeKillsGlorySeekerBeforeItDealsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new DaruCavalier());
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void searchTakesOnlyOneCopyRevealsItAndShuffles() {
        castDaruCavalier();
        DaruCavalier first = new DaruCavalier();
        DaruCavalier second = new DaruCavalier();
        GlorySeeker other = new GlorySeeker();
        harness.setLibrary(player1, List.of(first, second, other));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(second, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("reveals Daru Cavalier")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void acceptingSearchMayFailToFindEvenWhenMatchingCardExists() {
        castDaruCavalier();
        DaruCavalier copy = new DaruCavalier();
        GlorySeeker other = new GlorySeeker();
        harness.setLibrary(player1, List.of(copy, other));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Daru Cavalier");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(copy, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void searchStillResolvesAfterSourceLeavesBattlefield() {
        castDaruCavalier();
        DaruCavalier copy = new DaruCavalier();
        harness.setLibrary(player1, List.of(copy));

        resolveAllTriggers();
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(copy);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castDaruCavalier() {
        harness.setHand(player1, List.of(new DaruCavalier()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

}
