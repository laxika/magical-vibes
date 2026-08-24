package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimalAdversary.class, Forest.class, GrizzlyBears.class})
class PrimalAdversaryTest extends BaseCardTest {

    @Test
    void paysTwiceAndAnimatesUpToTwoLandsYouControl() {
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent thirdForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimalAdversary()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(firstForest.getId(), secondForest.getId(), thirdForest.getId())
                .doesNotContain(opponentForest.getId());

        harness.handlePermanentChosen(player1, firstForest.getId());
        harness.handlePermanentChosen(player1, secondForest.getId());
        harness.passBothPriorities();

        Permanent adversary = findAdversary();
        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertAnimatedForest(firstForest);
        assertAnimatedForest(secondForest);
        assertThat(gqs.isCreature(gd, thirdForest)).isFalse();
        assertThat(gqs.isCreature(gd, opponentForest)).isFalse();
    }

    @Test
    void decliningPaymentDoesNothing() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new PrimalAdversary()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        Permanent adversary = findAdversary();
        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    private void assertAnimatedForest(Permanent forest) {
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.WOLF);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
    }

    private Permanent findAdversary() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof PrimalAdversary)
                .findFirst()
                .orElseThrow();
    }
}
