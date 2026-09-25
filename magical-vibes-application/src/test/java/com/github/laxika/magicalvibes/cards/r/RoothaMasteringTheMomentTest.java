package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoothaMasteringTheMoment.class, Divination.class, GrizzlyBears.class, ThinkTwice.class})
class RoothaMasteringTheMomentTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying and hasty Elemental sized by the greatest instant or sorcery mana value")
    void createsTokenSizedByGreatestSpellManaValue() {
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());
        harness.setHand(player1, List.of(new ThinkTwice(), new Divination()));
        harness.setLibrary(player1, List.of());
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.HASTE);
    }

    @Test
    @DisplayName("Does not trigger without an instant or sorcery cast this turn")
    void doesNotTriggerWithoutMatchingSpell() {
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());
        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a creature spell")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new RoothaMasteringTheMoment());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
