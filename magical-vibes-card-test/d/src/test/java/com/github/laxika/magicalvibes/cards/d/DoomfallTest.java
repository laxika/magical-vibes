package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Doomfall.class, Forest.class, GiantSpider.class, GrizzlyBears.class, ManorGargoyle.class, Peek.class})
class DoomfallTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0: opponent with one creature has it exiled automatically")
    void modeExileSingleCreatureAutoExiled() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Doomfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mode 0: opponent with multiple creatures is prompted, then chosen creature is exiled")
    void modeExileMultipleCreaturesChooses() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        harness.setHand(player1, List.of(new Doomfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DestroyChosenCreature.class);

        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Mode 0: exile ignores indestructible (unlike a destroy edict)")
    void modeExileIgnoresIndestructible() {
        // Manor Gargoyle has defender, so its static ability makes it indestructible.
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player2, new ManorGargoyle());

        harness.setHand(player1, List.of(new Doomfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(gargoyle.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Manor Gargoyle"));
    }

    @Test
    @DisplayName("Mode 0: no effect when opponent has no creatures")
    void modeExileNoCreatures() {
        harness.setHand(player1, List.of(new Doomfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mode 1: chosen nonland card is exiled from the opponent's hand")
    void modeHandExileNonlandCard() {
        Card creature = new GrizzlyBears();
        Card peek = new Peek();
        harness.setHand(player2, List.of(creature, peek));

        harness.setHand(player1, List.of(new Doomfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        // Choose Grizzly Bears (index 0)
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Peek");
    }

    @Test
    @DisplayName("Mode 1: land cards cannot be chosen")
    void modeHandExileLandExcluded() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player2, List.of(creature, land));

        harness.setHand(player1, List.of(new Doomfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);
    }

    @Test
    @DisplayName("Creature-exile mode cannot target its caster")
    void creatureModeCannotTargetCaster() {
        harness.setHand(player1, List.of(new Doomfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hand-exile mode cannot target its caster")
    void handModeCannotTargetCaster() {
        harness.setHand(player1, List.of(new Doomfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hand-exile mode resolves without a choice when the opponent's hand is empty")
    void handModeEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Doomfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Doomfall");
    }

    @Test
    @DisplayName("An all-land hand is revealed but no card is exiled")
    void handModeOnlyLands() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Doomfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("reveals their hand")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Doomfall");
    }

    @Test
    @DisplayName("Caster can exile an instant but cannot choose a land or decline")
    void handModeExilesInstantAndRequiresValidChoice() {
        harness.setHand(player2, List.of(new Peek(), new Forest()));
        harness.setHand(player1, List.of(new Doomfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("reveals their hand")).isTrue();
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);

        harness.assertNotInHand(player2, "Peek");
        harness.assertNotInGraveyard(player2, "Peek");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).containsExactly("Peek");
        harness.assertInGraveyard(player1, "Doomfall");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
