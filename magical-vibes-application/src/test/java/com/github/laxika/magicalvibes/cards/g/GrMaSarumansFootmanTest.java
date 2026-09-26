package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrMaSarumansFootman.class, GrizzlyBears.class, Forest.class, Divination.class})
class GrMaSarumansFootmanTest extends BaseCardTest {

    @Test
    @DisplayName("Gríma cannot be blocked")
    void cannotBeBlocked() {
        Permanent grima = addCreatureReady(player1, new GrMaSarumansFootman());
        grima.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Combat damage searches the damaged player's library")
    void combatDamageSearchesDamagedPlayersLibrary() {
        Card forest = new Forest();
        Card divination = new Divination();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(forest, divination));

        attackWithGrima();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).containsExactly(divination);
    }

    @Test
    @DisplayName("The controller may cast the found spell without paying its mana cost")
    void castsFoundSpellWithoutPaying() {
        Card forest = new Forest();
        Card divination = new Divination();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(forest, divination));

        attackWithGrima();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(divination.getId())
                && entry.getEntryType() == StackEntryType.SORCERY_SPELL
                && entry.getControllerId().equals(player1.getId()));
    }

    private void attackWithGrima() {
        Permanent grima = addCreatureReady(player1, new GrMaSarumansFootman());
        grima.setAttacking(true);
        resolveCombat();
    }
}
