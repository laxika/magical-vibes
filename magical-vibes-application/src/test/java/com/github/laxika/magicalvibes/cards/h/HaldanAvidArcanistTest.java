package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaldanAvidArcanist.class, Forest.class, GrizzlyBears.class, Shock.class})
class HaldanAvidArcanistTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Pako")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card pako = namedCard("Pako, Arcane Retriever");
        harness.setLibrary(player2, List.of(pako));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new HaldanAvidArcanist());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(pako);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Plays fetch-counter lands and casts fetch-counter noncreature spells with any mana")
    void playsLandsAndCastsNoncreatureSpellsFromFetchCounters() {
        harness.addToBattlefield(player1, new HaldanAvidArcanist());
        Card land = new Forest();
        Card spell = new Shock();
        Card creature = new GrizzlyBears();
        gd.addToExileWithFetchCounter(player2.getId(), land, player1.getId());
        gd.addToExileWithFetchCounter(player2.getId(), spell, player1.getId());
        gd.addToExileWithFetchCounter(player2.getId(), creature, player1.getId());

        prepareMainPhase();
        gs.playCardFromExile(gd, player1, land.getId(), null, null);
        harness.assertOnBattlefield(player1, "Forest");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not grant permission for cards without fetch counters or exiled by another player")
    void restrictsPermissionToFetchCardsExiledByController() {
        harness.addToBattlefield(player1, new HaldanAvidArcanist());
        Card unmarked = new Shock();
        Card opponentExiled = new Shock();
        gd.addToExile(player1.getId(), unmarked);
        gd.addToExileWithFetchCounter(player2.getId(), opponentExiled, player2.getId());

        prepareMainPhase();
        assertThatThrownBy(() -> harness.castFromExile(player1, unmarked.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player1, opponentExiled.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
