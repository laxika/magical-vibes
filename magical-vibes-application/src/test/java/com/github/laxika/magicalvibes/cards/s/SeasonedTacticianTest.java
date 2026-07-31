package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeasonedTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {3} and exiling the top four library cards prompts for a source choice")
    void activateExilesTopFourAndPromptsForSource() {
        harness.addToBattlefield(player1, new SeasonedTactician());
        GameData gd = harness.getGameData();
        addReadyGoblin(player2);

        List<Card> library = gd.playerDecks.get(player1.getId());
        List<Card> topFour = List.copyOf(library.subList(0, 4));
        int deckBefore = library.size();
        int exileBefore = gd.exiledCards.size();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 4);
        assertThat(gd.exiledCards).hasSize(exileBefore + 4);
        assertThat(gd.exiledCards).extracting(e -> e.card().getId())
                .containsAll(topFour.stream().map(Card::getId).toList());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source")
    void preventsDamageFromChosenSource() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SeasonedTactician());
        Permanent goblin = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A different source still deals damage; the shield is untouched")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SeasonedTactician());
        Permanent chosen = addReadyGoblin(player2);
        Permanent other = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Cannot activate with fewer than four cards in library")
    void cannotActivateWithTooFewLibraryCards() {
        harness.addToBattlefield(player1, new SeasonedTactician());
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).subList(3, gd.playerDecks.get(player1.getId()).size()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to exile");
    }

    private Permanent addReadyGoblin(Player player) {
        Permanent perm = new Permanent(new GoblinPiker());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
