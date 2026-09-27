package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwindlersScheme.class, GrizzlyBears.class, CounselOfTheSoratami.class})
class SwindlersSchemeTest extends BaseCardTest {

    @Test
    @DisplayName("A matching revealed creature counters the spell and may be cast by its opponent")
    void matchingCardTypeCountersSpellAndOffersFreeCast() {
        harness.addToBattlefield(player1, new SwindlersScheme());
        GrizzlyBears revealed = new GrizzlyBears();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed));

        castOpponentCreature(spell);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveRemainingStack();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(revealed.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A revealed card with no shared card type does not counter the spell")
    void nonmatchingCardTypeDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new SwindlersScheme());
        GrizzlyBears revealed = new GrizzlyBears();
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        harness.setLibrary(player1, List.of(revealed));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castSorcery(player2, 0, 0);

        harness.handleMayAbilityChosen(player1, true);
        resolveRemainingStack();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .extracting(Card::getId)
                .isEqualTo(revealed.getId());
    }

    @Test
    @DisplayName("Declining the trigger leaves the opponent's spell to resolve")
    void decliningDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new SwindlersScheme());
        GrizzlyBears revealed = new GrizzlyBears();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed));

        castOpponentCreature(spell);
        harness.handleMayAbilityChosen(player1, false);
        resolveRemainingStack();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .extracting(Card::getId)
                .isEqualTo(revealed.getId());
    }

    private void castOpponentCreature(GrizzlyBears spell) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    private void resolveRemainingStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
