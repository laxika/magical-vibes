package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.w.WallOfBlossoms;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TalionTheKindlyLord.class, Fireball.class, GrizzlyBears.class, SuntailHawk.class,
        WallOfBlossoms.class})
class TalionTheKindlyLordTest extends BaseCardTest {

    private void castTalion(int chosenNumber) {
        harness.setHand(player1, List.of(new TalionTheKindlyLord()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, Integer.toString(chosenNumber));
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("As Talion enters, choosing a number is required")
    void enteringRequiresNumberChoice() {
        harness.setHand(player1, List.of(new TalionTheKindlyLord()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOf(com.github.laxika.magicalvibes.model.ChoiceContext.NumberChoice.class);
    }

    @Test
    @DisplayName("Matching mana value, power, or toughness makes the opponent lose life and draws a card")
    void matchingSpellTriggersTalion() {
        Card drawCard = new SuntailHawk();
        harness.setLibrary(player1, List.of(drawCard));
        castTalion(2);
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    @Test
    @DisplayName("A spell's toughness can match Talion's chosen number")
    void toughnessMatchTriggersTalion() {
        castTalion(4);
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new WallOfBlossoms()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("A chosen X value counts toward a spell's mana value")
    void chosenXValueMatchesTalion() {
        Card drawCard = new SuntailHawk();
        harness.setLibrary(player1, List.of(drawCard));
        castTalion(4);
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new Fireball()));
        harness.addMana(player2, ManaColor.RED, 4);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castSorcery(player2, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    @Test
    @DisplayName("Nonmatching spells do not trigger Talion")
    void nonmatchingSpellDoesNotTriggerTalion() {
        castTalion(2);
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }
}
