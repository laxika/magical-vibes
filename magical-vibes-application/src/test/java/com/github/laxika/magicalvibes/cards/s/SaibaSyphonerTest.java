package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaibaSyphoner.class, Shock.class, DoomBlade.class, Cancel.class, Divination.class, TurnToFrog.class})
class SaibaSyphonerTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {2} less when there are no instant or sorcery cards in hand")
    void costsLessWithNoInstantOrSorceryInHand() {
        harness.setHand(player1, List.of(new SaibaSyphoner()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce its cost when an instant or sorcery is in hand")
    void doesNotCostLessWithInstantOrSorceryInHand() {
        harness.setHand(player1, List.of(new SaibaSyphoner(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Goes to the graveyard when countered before entering the battlefield")
    void doesNotShuffleWhenCountered() {
        SaibaSyphoner saiba = new SaibaSyphoner();
        harness.setHand(player1, List.of(saiba));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, saiba.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Saiba Syphoner");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Returns a target instant or sorcery from the graveyard to hand")
    void returnsTargetInstantOrSorceryToHand() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new SaibaSyphoner()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Shuffles into its owner's library instead of dying")
    void shufflesIntoLibraryInsteadOfDying() {
        Permanent saiba = harness.addToBattlefieldAndReturn(player1, new SaibaSyphoner());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castAndResolveInstant(player2, 0, saiba.getId());

        harness.assertNotOnBattlefield(player1, "Saiba Syphoner");
        harness.assertNotInGraveyard(player1, "Saiba Syphoner");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(saiba.getCard());
    }

    @Test
    void sorceryInHandPreventsCostReduction() {
        harness.setHand(player1, List.of(new SaibaSyphoner(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void returnsSorceryFromGraveyard() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setHand(player1, List.of(new SaibaSyphoner()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(divination);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotReturnCreatureOrOpponentsInstant() {
        SaibaSyphoner creature = new SaibaSyphoner();
        Shock opponentInstant = new Shock();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(opponentInstant));
        harness.setHand(player1, List.of(new SaibaSyphoner()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Saiba Syphoner");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentInstant);
    }

    @Test
    void goesToGraveyardWhenItHasLostAllAbilities() {
        Permanent saiba = harness.addToBattlefieldAndReturn(player1, new SaibaSyphoner());
        harness.setHand(player2, List.of(new TurnToFrog(), new DoomBlade()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveInstant(player2, 0, saiba.getId());
        harness.castAndResolveInstant(player2, 0, saiba.getId());

        harness.assertNotOnBattlefield(player1, "Saiba Syphoner");
        harness.assertInGraveyard(player1, "Saiba Syphoner");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(saiba.getCard());
    }
}
