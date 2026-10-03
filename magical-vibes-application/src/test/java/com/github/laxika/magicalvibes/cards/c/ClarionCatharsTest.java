package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FatefulAbsence;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClarionCathars.class, FatefulAbsence.class})
class ClarionCatharsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Clarion Cathars puts its ETB trigger on the stack")
    void castingPutsEtbOnStack() {
        harness.setHand(player1, List.of(new ClarionCathars()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Clarion Cathars");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Clarion Cathars creates a 1/1 white Human creature token")
    void etbCreatesHumanToken() {
        harness.setHand(player1, List.of(new ClarionCathars()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Human");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("A non-cast entry creates exactly one token for the entering creature's controller")
    void nonCastEntryCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new ClarionCathars());

        assertThat(countPermanents(player2, "Human")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Human")).isEqualTo(1);
        assertThat(countPermanents(player1, "Human")).isZero();
        Permanent token = findPermanent(player2, "Human");
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The token is still created when Clarion Cathars dies before its trigger resolves")
    void triggerResolvesAfterSourceDies() {
        harness.setHand(player1, List.of(new ClarionCathars(), new FatefulAbsence()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent cathars = findPermanent(player1, "Clarion Cathars");
        harness.castInstant(player1, 0, cathars.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clarion Cathars")).isZero();
        assertThat(countPermanents(player1, "Human")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
        assertThat(countPermanents(player2, "Human")).isZero();
    }
}
