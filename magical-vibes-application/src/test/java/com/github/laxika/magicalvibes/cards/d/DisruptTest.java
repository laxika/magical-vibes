package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.f.Firestorm;
import com.github.laxika.magicalvibes.cards.p.ParadigmShift;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Disrupt.class, Firestorm.class, BenalishInfantry.class, ParadigmShift.class})
class DisruptTest extends BaseCardTest {

    private void stockLibrary(Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new BenalishInfantry());
        }
        harness.setLibrary(player, deck);
    }

    /** Player1 casts Firestorm at both players; player2 responds with Disrupt targeting it. */
    private Firestorm castFirestormDisruptedBy(Disrupt disrupt, int player1ExtraMana) {
        Firestorm firestorm = new Firestorm();
        harness.setHand(player1, List.of(firestorm, new BenalishInfantry(), new BenalishInfantry()));
        harness.addMana(player1, ManaColor.RED, 1 + player1ExtraMana);

        harness.setHand(player2, List.of(disrupt));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstantForXWithDiscards(player1, 0, 2,
                List.of(player2.getId(), player1.getId()), List.of(1, 2));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firestorm.getId());
        return firestorm;
    }

    @Test
    @DisplayName("Counters the target instant when its controller cannot pay {1}")
    void countersWhenControllerCannotPay() {
        stockLibrary(player2, 3);

        castFirestormDisruptedBy(new Disrupt(), 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Firestorm");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Spell resolves when its controller pays {1}")
    void notCounteredWhenControllerPays() {
        stockLibrary(player2, 3);

        Firestorm firestorm = castFirestormDisruptedBy(new Disrupt(), 1);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, firestorm.getName());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Draws a card regardless of whether the spell was countered")
    void drawsACard() {
        stockLibrary(player2, 3);

        castFirestormDisruptedBy(new Disrupt(), 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Counters the target spell when its controller declines to pay {1}")
    void countersWhenControllerDeclinesToPay() {
        stockLibrary(player2, 1);

        castFirestormDisruptedBy(new Disrupt(), 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Firestorm");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can target a sorcery spell")
    void canTargetSorcerySpell() {
        stockLibrary(player2, 1);

        ParadigmShift paradigmShift = new ParadigmShift();
        harness.setHand(player1, List.of(paradigmShift));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Disrupt()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, paradigmShift.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(paradigmShift);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        BenalishInfantry infantry = new BenalishInfantry();
        harness.setHand(player1, List.of(infantry));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new Disrupt()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, infantry.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles without drawing when its target leaves the stack")
    void fizzlesWithoutDrawingWhenTargetLeavesStack() {
        stockLibrary(player2, 1);

        Firestorm firestorm = castFirestormDisruptedBy(new Disrupt(), 0);
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(firestorm.getId()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Disrupt");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
