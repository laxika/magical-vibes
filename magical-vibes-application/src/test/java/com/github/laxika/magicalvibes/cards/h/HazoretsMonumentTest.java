package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HazoretsMonumentTest extends BaseCardTest {

    // ===== Red creature cost reduction =====

    @Test
    @DisplayName("Red creature spells cost {1} less with Hazoret's Monument on the battlefield")
    void redCreatureCostsOneLess() {
        harness.addToBattlefield(player1, new HazoretsMonument());
        // Hill Giant costs {3}{R} — with {1} reduction it should cost {2}{R} = 3 mana
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Red creature reduction is only {1} — one less mana still cannot pay")
    void redCreatureReductionIsExactlyOne() {
        harness.addToBattlefield(player1, new HazoretsMonument());
        // Hill Giant reduced to {2}{R} = 3 mana; only 2 mana is not enough
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-red creature spells are not reduced")
    void nonRedCreatureNotReduced() {
        harness.addToBattlefield(player1, new HazoretsMonument());
        // Grizzly Bears costs {1}{G} — green, so no reduction; only {G} cannot pay
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Loot trigger on creature cast =====

    @Test
    @DisplayName("Casting a creature spell triggers the may-loot prompt")
    void creatureCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new HazoretsMonument());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting discards a card then draws a card")
    void acceptDiscardsThenDraws() {
        harness.addToBattlefield(player1, new HazoretsMonument());
        HillGiant toDiscard = new HillGiant();
        harness.setHand(player1, List.of(new GrizzlyBears(), toDiscard));
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        // Cast Grizzly Bears — hand becomes [Hill Giant]
        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability resolves, then prompts discard
        harness.passBothPriorities();
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        // Discard the Hill Giant — draw follows the discard (rummage)
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Declining the loot draws and discards nothing")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new HazoretsMonument());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Hazoret's Monument"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    // ===== Noncreature spells do not trigger =====

    @Test
    @DisplayName("Casting a noncreature spell does not trigger the loot")
    void noncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new HazoretsMonument());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    // ===== Helpers =====

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
