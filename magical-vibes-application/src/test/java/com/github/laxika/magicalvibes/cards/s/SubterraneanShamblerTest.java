package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SubterraneanShambler.class})
class SubterraneanShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 1 damage to nonflying creatures only")
    void etbDamagesNonflyingCreaturesOnly() {
        Permanent nonflying = harness.addToBattlefieldAndReturn(player2, creature("Ground Creature", 3, 3));
        Permanent flying = harness.addToBattlefieldAndReturn(player2, creature("Flying Creature", 3, 3, Keyword.FLYING));

        castShambler();

        assertThat(nonflying.getMarkedDamage()).isEqualTo(1);
        assertThat(flying.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Leaves-the-battlefield trigger deals 1 damage to nonflying creatures only")
    void leavesTheBattlefieldDamagesNonflyingCreaturesOnly() {
        Permanent nonflying = harness.addToBattlefieldAndReturn(player2, creature("Ground Creature", 3, 3));
        Permanent flying = harness.addToBattlefieldAndReturn(player2, creature("Flying Creature", 3, 3, Keyword.FLYING));
        Permanent shambler = castShambler();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, shambler));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(nonflying.getMarkedDamage()).isEqualTo(2);
        assertThat(flying.getMarkedDamage()).isZero();
    }

    private Permanent castShambler() {
        harness.setHand(player1, List.of(new SubterraneanShambler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Subterranean Shambler");
    }

    private Card creature(String name, int power, int toughness, Keyword... keywords) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{3}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setKeywords(Set.of(keywords));
        return card;
    }
}
