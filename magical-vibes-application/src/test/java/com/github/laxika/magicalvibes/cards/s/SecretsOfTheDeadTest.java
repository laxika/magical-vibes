package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LingeringSouls;
import com.github.laxika.magicalvibes.cards.r.RayOfRevelation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SecretsOfTheDead.class, LingeringSouls.class, SomberwaldDryad.class,
        SqueeTheImmortal.class, RayOfRevelation.class})
class SecretsOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when casting a flashback spell")
    void drawsCardOnFlashbackCast() {
        harness.addToBattlefield(player1, new SecretsOfTheDead());
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Secrets of the Dead");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws a card when casting a spell from graveyard without flashback")
    void drawsCardOnGraveyardCast() {
        harness.addToBattlefield(player1, new SecretsOfTheDead());
        harness.setGraveyard(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger when casting a spell from hand")
    void doesNotTriggerOnSpellFromHand() {
        harness.addToBattlefield(player1, new SecretsOfTheDead());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new SomberwaldDryad(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Does not trigger when opponent casts spell from graveyard")
    void doesNotTriggerOnOpponentGraveyardCast() {
        harness.addToBattlefield(player1, new SecretsOfTheDead());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setGraveyard(player2, List.of(new LingeringSouls()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFlashback(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Multiple Secrets of the Dead each draw a card")
    void multipleSecretsOfTheDeadTrigger() {
        harness.addToBattlefield(player1, new SecretsOfTheDead());
        harness.addToBattlefield(player1, new SecretsOfTheDead());
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(3);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Draw trigger resolves before the spell cast from the graveyard")
    void drawsBeforeGraveyardSpellResolves() {
        harness.addToBattlefield(player1, new SecretsOfTheDead());
        harness.setHand(player1, List.of());
        SomberwaldDryad drawnCard = new SomberwaldDryad();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A queued draw trigger survives Secrets of the Dead leaving the battlefield")
    void drawTriggerSurvivesSourceLeaving() {
        harness.addToBattlefield(player1, new SecretsOfTheDead());
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFlashback(player1, 0);
        harness.setHand(player2, List.of(new RayOfRevelation()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Secrets of the Dead"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Secrets of the Dead");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting from exile does not trigger Secrets of the Dead")
    void doesNotTriggerOnCastFromExile() {
        harness.addToBattlefield(player1, new SecretsOfTheDead());
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setExile(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromExile(player1, squee.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.assertOnBattlefield(player1, "Squee, the Immortal");
    }
}
