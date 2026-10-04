package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.n.NightsWhisper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrMaSarumansFootman.class, ElvishMystic.class, NightsWhisper.class})
class GrMaSarumansFootmanTest extends BaseCardTest {

    @Test
    @DisplayName("Grima cannot be blocked")
    void cannotBeBlocked() {
        Permanent grima = addCreatureReady(player1, new GrMaSarumansFootman());
        grima.setAttacking(true);
        addCreatureReady(player2, new ElvishMystic());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Combat damage searches the damaged player's library")
    void combatDamageSearchesDamagedPlayersLibrary() {
        Card skipped = new ElvishMystic();
        Card spell = new NightsWhisper();
        harness.setLibrary(player2, List.of(skipped, spell));

        attackWithGrima();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).containsExactly(spell);
    }

    @Test
    @DisplayName("The controller may cast the found spell without paying its mana cost")
    void castsFoundSpellWithoutPaying() {
        Card skipped = new ElvishMystic();
        Card spell = new NightsWhisper();
        harness.setLibrary(player2, List.of(skipped, spell));

        attackWithGrima();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(spell.getId())
                && entry.getEntryType() == StackEntryType.SORCERY_SPELL
                && entry.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Cards enter exile while the controller chooses whether to cast")
    void cardsEnterExileDuringCastChoice() {
        Card skipped = new ElvishMystic();
        Card spell = new NightsWhisper();
        harness.setLibrary(player2, List.of(skipped, spell));

        attackWithGrima();

        assertThat(gd.findExiledCard(skipped.getId())).isSameAs(skipped);
        assertThat(gd.findExiledCard(spell.getId())).isSameAs(spell);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining returns all exiled cards below the untouched library")
    void decliningReturnsCardsToBottom() {
        Card skipped = new ElvishMystic();
        Card spell = new NightsWhisper();
        Card untouched = new ElvishMystic();
        harness.setLibrary(player2, List.of(skipped, spell, untouched));

        attackWithGrima();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player2.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skipped, spell);
        assertThat(gd.findExiledCard(skipped.getId())).isNull();
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("A library with no instant or sorcery is returned in full")
    void noEligibleSpellReturnsLibrary() {
        Card first = new ElvishMystic();
        Card second = new ElvishMystic();
        harness.setLibrary(player2, List.of(first, second));

        attackWithGrima();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library finishes without a cast choice")
    void emptyLibraryFinishes() {
        harness.setLibrary(player2, List.of());

        attackWithGrima();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The borrowed spell resolves for Grima's controller and goes to its owner's graveyard")
    void borrowedSpellResolvesForController() {
        Card skipped = new ElvishMystic();
        Card spell = new NightsWhisper();
        Card laterSpell = new NightsWhisper();
        Card firstDraw = new ElvishMystic();
        Card secondDraw = new ElvishMystic();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(skipped, spell, laterSpell));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        attackWithGrima();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(laterSpell, skipped);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    private void attackWithGrima() {
        Permanent grima = addCreatureReady(player1, new GrMaSarumansFootman());
        grima.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
    }
}
