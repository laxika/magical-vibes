package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FrodoAdventurousHobbit.class)
class FrodoAdventurousHobbitTest extends BaseCardTest {

    @Test
    void partnerWithLetsTargetPlayerSearchForSam() {
        Card sam = namedCard("Sam, Loyal Attendant");
        harness.setLibrary(player2, List.of(sam));
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new FrodoAdventurousHobbit());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPlayerIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(sam);
    }

    @Test
    void attackingAfterSecondRingTemptationDrawsIfFrodoIsRingBearer() {
        Permanent frodo = addCreatureReady(player1, new FrodoAdventurousHobbit());
        gd.ringLevels.put(player1.getId(), 1);
        gd.ringBearerIds.put(player1.getId(), frodo.getId());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.ringLevels).containsEntry(player1.getId(), 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTemptTheRingWithoutThreeLifeGained() {
        addCreatureReady(player1, new FrodoAdventurousHobbit());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.ringLevels).doesNotContainKey(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
