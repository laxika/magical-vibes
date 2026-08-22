package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaughingJasperFlint.class, GrizzlyBears.class, Threaten.class})
class LaughingJasperFlintTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one card per outlaw you control, including controlled creatures you do not own")
    void exilesCardsForControlledOutlaws() {
        Permanent jasper = addCreatureReady(player1, new LaughingJasperFlint());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent stolenCreature = addCreatureReady(player2, new GrizzlyBears());
        takeControlUntilEndOfTurn(stolenCreature);

        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, third));

        advanceToUpkeep(player1);
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(jasper.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Casts cards exiled by the upkeep ability using mana of any type that turn")
    void castsExiledSpellWithAnyManaType() {
        Permanent jasper = addCreatureReady(player1, new LaughingJasperFlint());
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiled));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiled);
        assertThat(gd.getCardsExiledByPermanent(jasper.getId())).isEmpty();
    }

    private void takeControlUntilEndOfTurn(Permanent target) {
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
