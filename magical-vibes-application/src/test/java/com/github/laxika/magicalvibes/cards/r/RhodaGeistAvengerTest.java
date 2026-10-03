package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhodaGeistAvenger.class, GrizzlyBears.class})
class RhodaGeistAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Timin")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card timin = namedCard("Timin, Youthful Geist");
        harness.setLibrary(player2, List.of(timin));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new RhodaGeistAvenger());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(timin);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A nonattacking opponent creature becoming tapped puts a counter on Rhoda")
    void nonattackingOpponentCreatureTapAddsCounter() {
        Permanent rhoda = harness.addToBattlefieldAndReturn(player1, new RhodaGeistAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(bears);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(rhoda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent creature declared as an attacker does not put a counter on Rhoda")
    void attackingOpponentCreatureDoesNotAddCounter() {
        Permanent rhoda = harness.addToBattlefieldAndReturn(player1, new RhodaGeistAvenger());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(rhoda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
