package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DreamTwist;
import com.github.laxika.magicalvibes.cards.t.TributeToHunger;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkaabRuinator.class, WalkingCorpse.class, DreamTwist.class, TributeToHunger.class})
class SkaabRuinatorTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast from hand by exiling 3 creature cards from graveyard")
    void castFromHandExilesThreeCreatures() {
        WalkingCorpse corpse1 = new WalkingCorpse();
        WalkingCorpse corpse2 = new WalkingCorpse();
        WalkingCorpse corpse3 = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse1, corpse2, corpse3));

        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");

        // All 3 creature cards exiled
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot cast from hand with fewer than 3 creature cards in graveyard")
    void cannotCastWithFewerThanThreeCreatures() {
        WalkingCorpse corpse1 = new WalkingCorpse();
        WalkingCorpse corpse2 = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse1, corpse2));

        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile non-creature cards to pay the additional cost")
    void cannotExileNonCreatureCards() {
        WalkingCorpse corpse1 = new WalkingCorpse();
        WalkingCorpse corpse2 = new WalkingCorpse();
        WalkingCorpse corpse3 = new WalkingCorpse();
        DreamTwist twist = new DreamTwist();
        // Need 3 creatures for playability, but we'll try to exile Dream Twist instead of corpse3
        harness.setGraveyard(player1, List.of(corpse1, corpse2, corpse3, twist));

        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Try to exile indices 0, 1, 3 — index 3 is Dream Twist (instant), not a creature
        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot use duplicate graveyard indices")
    void cannotUseDuplicateIndices() {
        WalkingCorpse corpse1 = new WalkingCorpse();
        WalkingCorpse corpse2 = new WalkingCorpse();
        WalkingCorpse corpse3 = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse1, corpse2, corpse3));

        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    @DisplayName("Resolves as a creature on the battlefield")
    void resolvesOnBattlefield() {
        WalkingCorpse corpse1 = new WalkingCorpse();
        WalkingCorpse corpse2 = new WalkingCorpse();
        WalkingCorpse corpse3 = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse1, corpse2, corpse3));

        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skaab Ruinator");
    }

    @Test
    @DisplayName("Can cast from graveyard by exiling 3 other creature cards")
    void castFromGraveyardExilesThreeCreatures() {
        WalkingCorpse corpse1 = new WalkingCorpse();
        WalkingCorpse corpse2 = new WalkingCorpse();
        WalkingCorpse corpse3 = new WalkingCorpse();
        SkaabRuinator ruinator = new SkaabRuinator();
        harness.setGraveyard(player1, List.of(ruinator, corpse1, corpse2, corpse3));

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Cast from graveyard index 0 (ruinator).
        // After removing ruinator from graveyard, corpses are at indices 0, 1, 2.
        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");

        // All 3 corpses exiled, ruinator removed from graveyard (on stack)
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast from graveyard without 3 other creature cards")
    void cannotCastFromGraveyardWithoutEnoughCreatures() {
        WalkingCorpse corpse1 = new WalkingCorpse();
        WalkingCorpse corpse2 = new WalkingCorpse();
        SkaabRuinator ruinator = new SkaabRuinator();
        harness.setGraveyard(player1, List.of(ruinator, corpse1, corpse2));

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting from graveyard does not exile the card after resolution (unlike flashback)")
    void castFromGraveyardDoesNotExile() {
        WalkingCorpse corpse1 = new WalkingCorpse();
        WalkingCorpse corpse2 = new WalkingCorpse();
        WalkingCorpse corpse3 = new WalkingCorpse();
        SkaabRuinator ruinator = new SkaabRuinator();
        harness.setGraveyard(player1, List.of(ruinator, corpse1, corpse2, corpse3));

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        // Should be on the battlefield, not exiled
        harness.assertOnBattlefield(player1, "Skaab Ruinator");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Skaab Ruinator"));
    }

    @Test
    @DisplayName("A graveyard cast with an invalid exile selection is rejected before any cost is paid")
    void rejectedGraveyardCastLeavesStateUntouched() {
        WalkingCorpse corpse1 = new WalkingCorpse();
        WalkingCorpse corpse2 = new WalkingCorpse();
        WalkingCorpse corpse3 = new WalkingCorpse();
        DreamTwist twist = new DreamTwist();
        SkaabRuinator ruinator = new SkaabRuinator();
        harness.setGraveyard(player1, List.of(ruinator, corpse1, corpse2, corpse3, twist));

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Post-removal index 3 is Dream Twist — an illegal exile for the creature-card cost.
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        // Rejected casting must leave the original graveyard and mana intact.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Skaab Ruinator");
    }

    @Test
    @DisplayName("Casting from graveyard requires sorcery-speed timing")
    void castFromGraveyardRequiresSorceryTiming() {
        WalkingCorpse corpse1 = new WalkingCorpse();
        WalkingCorpse corpse2 = new WalkingCorpse();
        WalkingCorpse corpse3 = new WalkingCorpse();
        SkaabRuinator ruinator = new SkaabRuinator();
        harness.setGraveyard(player1, List.of(ruinator, corpse1, corpse2, corpse3));

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Force to a non-main phase
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting from graveyard still requires the normal mana cost")
    void cannotCastFromGraveyardWithoutMana() {
        SkaabRuinator ruinator = new SkaabRuinator();
        List<Card> graveyard = List.of(
                ruinator, new WalkingCorpse(), new WalkingCorpse(), new WalkingCorpse());
        harness.setGraveyard(player1, graveyard);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting a nonfirst graveyard card exiles exactly the selected other creatures")
    void castFromMiddleOfGraveyardPreservesUnselectedCards() {
        WalkingCorpse first = new WalkingCorpse();
        WalkingCorpse second = new WalkingCorpse();
        WalkingCorpse third = new WalkingCorpse();
        WalkingCorpse unselected = new WalkingCorpse();
        DreamTwist twist = new DreamTwist();
        SkaabRuinator ruinator = new SkaabRuinator();
        harness.setGraveyard(player1, List.of(first, ruinator, twist, second, unselected, third));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 1, List.of(4, 0, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(ruinator);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(twist, unselected);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Skaab Ruinator");
    }

    @Test
    @DisplayName("Cannot cast from graveyard during an opponent's main phase")
    void cannotCastFromGraveyardOnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(
                new SkaabRuinator(), new WalkingCorpse(), new WalkingCorpse(), new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot cast from graveyard while another spell is on the stack")
    void cannotCastFromGraveyardWithNonemptyStack() {
        harness.setGraveyard(player1, List.of(
                new SkaabRuinator(), new WalkingCorpse(), new WalkingCorpse(), new WalkingCorpse()));
        harness.setHand(player1, List.of(new DreamTwist()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("After dying a graveyard-cast Ruinator can be cast again by paying the cost again")
    void canCastAgainAfterDying() {
        SkaabRuinator ruinator = new SkaabRuinator();
        WalkingCorpse first = new WalkingCorpse();
        WalkingCorpse second = new WalkingCorpse();
        WalkingCorpse third = new WalkingCorpse();
        WalkingCorpse fourth = new WalkingCorpse();
        WalkingCorpse fifth = new WalkingCorpse();
        WalkingCorpse sixth = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(ruinator, first, second, third, fourth, fifth, sixth));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Skaab Ruinator");

        harness.setHand(player2, List.of(new TributeToHunger()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertNotOnBattlefield(player1, "Skaab Ruinator");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fourth, fifth, sixth, ruinator);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second, third);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 3, List.of(0, 1, 2));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth, fifth, sixth);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Skaab Ruinator");
    }
}
