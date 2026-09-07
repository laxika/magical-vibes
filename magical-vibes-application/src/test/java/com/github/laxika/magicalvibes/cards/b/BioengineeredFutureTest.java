package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BioengineeredFuture.class, Forest.class, GrizzlyBears.class})
class BioengineeredFutureTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Lander and gives entering creatures counters equal to lands entered this turn")
    void createsLanderAndCountsLandsEnteredThisTurn() {
        enterForest();
        enterForest();
        castBioengineeredFuture();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts later lands and does not affect creatures controlled by an opponent")
    void countsLaterLandsAndOnlyAffectsItsControllersCreatures() {
        castBioengineeredFuture();
        enterForest();

        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The Lander searches for a basic land and puts it onto the battlefield tapped")
    void landerSearchesForTappedBasicLand() {
        castBioengineeredFuture();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        Permanent lander = findPermanents(player1, "Lander").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lander), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId())
                        && permanent.isTapped()
                        && permanent.getCard().hasType(CardType.LAND));
    }

    private void castBioengineeredFuture() {
        harness.setHand(player1, List.of(new BioengineeredFuture()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void enterForest() {
        harness.enterBattlefieldAndReturn(player1, new Forest());
    }
}
