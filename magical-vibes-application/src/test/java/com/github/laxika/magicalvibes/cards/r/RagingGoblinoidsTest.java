package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElectrosBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RagingGoblinoids.class, ElectrosBolt.class})
class RagingGoblinoidsTest extends BaseCardTest {

    @Test
    @DisplayName("Mayhem casts Raging Goblinoids from the graveyard after it was discarded this turn")
    void mayhemCastsAfterDiscarding() {
        RagingGoblinoids card = new RagingGoblinoids();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getOriginalCard() == card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Mayhem cannot cast Raging Goblinoids from the graveyard before it was discarded")
    void mayhemRequiresDiscardThisTurn() {
        harness.setGraveyard(player1, List.of(new RagingGoblinoids()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mayhem still requires a main phase")
    void mayhemCannotBeCastDuringCombat() {
        prepareDiscardedCard();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Raging Goblinoids");
    }

    @Test
    @DisplayName("Mayhem cannot be cast during the opponent's turn")
    void mayhemCannotBeCastOnOpponentsTurn() {
        prepareDiscardedCard();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Raging Goblinoids");
    }

    @Test
    @DisplayName("Mayhem cannot be cast while another spell is on the stack")
    void mayhemRequiresEmptyStack() {
        prepareDiscardedCard();
        harness.setHand(player1, List.of(new RagingGoblinoids()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Raging Goblinoids");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Mayhem requires the full alternate mana cost")
    void mayhemRequiresEnoughMana() {
        RagingGoblinoids card = new RagingGoblinoids();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Raging Goblinoids");
    }

    @Test
    @DisplayName("Raging Goblinoids can attack immediately after being cast with mayhem")
    void mayhemCreatureHasHaste() {
        prepareDiscardedCard();
        harness.castAndResolveFlashback(player1, 0, null);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Mayhem cannot cast a different copy that was not discarded")
    void mayhemRequiresThisSpecificCardToHaveBeenDiscarded() {
        RagingGoblinoids discarded = new RagingGoblinoids();
        RagingGoblinoids other = new RagingGoblinoids();
        harness.setGraveyard(player1, List.of(discarded, other));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(discarded.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded, other);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("Mayhem cannot replace its red mana requirement with generic mana")
    void mayhemRequiresRedMana() {
        RagingGoblinoids card = new RagingGoblinoids();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Raging Goblinoids");
    }

    @Test
    @DisplayName("A creature that dies after being cast with mayhem cannot use the earlier discard again")
    void mayhemCannotRecastAfterDyingInTheSameTurn() {
        prepareDiscardedCard();
        harness.castAndResolveFlashback(player1, 0, null);
        harness.setHand(player1, List.of(new ElectrosBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0,
                harness.getPermanentId(player1, "Raging Goblinoids"));

        harness.assertNotOnBattlefield(player1, "Raging Goblinoids");
        harness.assertInGraveyard(player1, "Raging Goblinoids");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Raging Goblinoids");
    }

    private void prepareDiscardedCard() {
        RagingGoblinoids card = new RagingGoblinoids();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
