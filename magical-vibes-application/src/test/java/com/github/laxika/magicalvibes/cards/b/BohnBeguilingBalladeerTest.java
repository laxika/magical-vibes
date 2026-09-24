package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BohnBeguilingBalladeer.class, DarkRitual.class, Forest.class, GrizzlyBears.class})
class BohnBeguilingBalladeerTest extends BaseCardTest {

    @Test
    @DisplayName("Grants foretell to nonland cards in hand")
    void grantsForetellToNonlandCardsInHand() {
        addCreatureReady(player1, new BohnBeguilingBalladeer());
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(ritual, new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(ritual.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Goads a creature an opponent controls on the second spell")
    void goadsOpponentCreatureOnSecondSpell() {
        addCreatureReady(player1, new BohnBeguilingBalladeer());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
