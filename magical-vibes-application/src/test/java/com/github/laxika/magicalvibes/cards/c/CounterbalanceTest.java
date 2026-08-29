package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CounterbalanceTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger counters an opponent's spell when the mana values match")
    void matchingManaValueCountersSpell() {
        harness.addToBattlefield(player1, new Counterbalance());
        Card topCard = new GrizzlyBears();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        castOpponentCreature(spell, 2);

        harness.handleMayAbilityChosen(player1, true);
        resolveRemainingStack();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .extracting(card -> card.getId())
                .isEqualTo(topCard.getId());
    }

    @Test
    @DisplayName("A nonmatching mana value leaves the opponent's spell to resolve")
    void nonmatchingManaValueDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        HillGiant spell = new HillGiant();
        castOpponentCreature(spell, 5);

        harness.handleMayAbilityChosen(player1, true);
        resolveRemainingStack();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Declining the trigger leaves the opponent's spell to resolve")
    void decliningDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        GrizzlyBears spell = new GrizzlyBears();
        castOpponentCreature(spell, 2);

        harness.handleMayAbilityChosen(player1, false);
        resolveRemainingStack();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("The ability does not trigger for a spell cast by its controller")
    void controllerSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void castOpponentCreature(com.github.laxika.magicalvibes.model.Card card, int mana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(card));
        if (card instanceof HillGiant) {
            harness.addMana(player2, ManaColor.RED, 1);
            harness.addMana(player2, ManaColor.COLORLESS, mana - 1);
        } else {
            harness.addMana(player2, ManaColor.GREEN, mana);
        }
        harness.castCreature(player2, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    private void resolveRemainingStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
