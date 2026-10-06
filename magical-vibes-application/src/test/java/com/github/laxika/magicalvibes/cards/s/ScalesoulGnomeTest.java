package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScalesoulGnome.class, Forest.class, GrizzlyBears.class})
class ScalesoulGnomeTest extends BaseCardTest {

    @Test
    void discoversUsingCombatDamage() {
        Forest land = new Forest();
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, discovered));
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new ScalesoulGnome());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void conjuresDuplicateWhenSpellIsCastFromExile() {
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new ScalesoulGnome());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        prepareMainPhase(player1);

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getId()).isNotEqualTo(spell.getId());
        assertThat(duplicate.isTokenCard()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == spell);
    }

    @Test
    void conjuresDuplicateWhenLandIsPlayedFromExile() {
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new ScalesoulGnome());
        Forest land = new Forest();
        gd.addToExile(player1.getId(), land);
        gd.exilePlayPermissions.put(land.getId(), player1.getId());
        prepareMainPhase(player1);

        harness.castFromExile(player1, land.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getId()).isNotEqualTo(land.getId());
        assertThat(duplicate.isTokenCard()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land);
    }

    @Test
    void castingDiscoveredCardConjuresDuplicate() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new ScalesoulGnome());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getClass()).isEqualTo(discovered.getClass());
        assertThat(duplicate.getId()).isNotEqualTo(discovered.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == discovered);
    }

    @Test
    void discoverSkipsCardsAboveDamageAmountAndLeavesUnrevealedCardsOnTop() {
        ScalesoulGnome tooExpensive = new ScalesoulGnome();
        GrizzlyBears discovered = new GrizzlyBears();
        Forest unrevealed = new Forest();
        harness.setLibrary(player1, List.of(tooExpensive, discovered, unrevealed));
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new ScalesoulGnome());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, tooExpensive);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void playingCardsFromHandDoesNotConjureDuplicates() {
        GrizzlyBears spell = new GrizzlyBears();
        Forest land = new Forest();
        harness.setHand(player1, List.of(land, spell));
        addCreatureReady(player1, new ScalesoulGnome());
        prepareMainPhase(player1);

        harness.playLand(player1, 0);
        resolveAllTriggers();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == spell)
                .anyMatch(permanent -> permanent.getCard() == land);
    }

    @Test
    void opponentsExilePlaysDoNotConjureDuplicates() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new ScalesoulGnome());
        Forest land = new Forest();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setExile(player2, List.of(land, spell));
        gd.exilePlayPermissions.put(land.getId(), player2.getId());
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        prepareMainPhase(player2);

        harness.castFromExile(player2, land.getId());
        resolveAllTriggers();
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castFromExile(player2, spell.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == spell)
                .anyMatch(permanent -> permanent.getCard() == land);
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
