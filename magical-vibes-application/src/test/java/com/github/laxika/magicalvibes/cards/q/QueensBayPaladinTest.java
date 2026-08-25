package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireNoble;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QueensBayPaladin.class, VampireNoble.class, GrizzlyBears.class})
class QueensBayPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to one Vampire with a finality counter and loses life equal to its mana value")
    void etbReturnsVampireWithFinalityCounter() {
        Card vampire = new VampireNoble();
        Card nonVampire = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(vampire, nonVampire));
        harness.setLife(player1, 20);

        castPaladin();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(vampire.getId());

        harness.handleMultipleCardsChosen(player1, List.of(vampire.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanentByCardId(vampire.getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attack trigger returns a Vampire from your graveyard")
    void attackTriggerReturnsVampire() {
        Permanent paladin = new Permanent(new QueensBayPaladin());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(paladin);
        Card vampire = new VampireNoble();
        harness.setGraveyard(player1, List.of(vampire));
        harness.setLife(player1, 20);

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(vampire.getId()));
        harness.passBothPriorities();

        assertThat(findPermanentByCardId(vampire.getId()).getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The graveyard target is optional")
    void canChooseNoVampire() {
        Card vampire = new VampireNoble();
        harness.setGraveyard(player1, List.of(vampire));
        harness.setLife(player1, 20);

        castPaladin();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vampire Noble");
        harness.assertLife(player1, 20);
    }

    private void castPaladin() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new QueensBayPaladin()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
