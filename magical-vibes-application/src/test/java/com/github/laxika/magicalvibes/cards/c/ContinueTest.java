package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Continue.class, GrizzlyBears.class})
class ContinueTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to four target creatures put into the graveyard from the battlefield this turn")
    void returnsUpToFourTargetCreaturesFromThisTurn() {
        List<Permanent> creatures = addAndDestroyCreatures(5);
        List<UUID> targetIds = creatures.stream()
                .limit(4)
                .map(permanent -> permanent.getCard().getId())
                .toList();

        castContinue();
        harness.castInstant(player1, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(
                creatures.stream().map(permanent -> permanent.getCard().getId()).toList());
        harness.handleMultipleCardsChosen(player1, targetIds);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrderElementsOf(targetIds);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creatures.get(4).getCard().getId());
    }

    @Test
    @DisplayName("Offers only creature cards put into the graveyard from the battlefield this turn")
    void onlyOffersEligibleCreatureCards() {
        Card alreadyInGraveyard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(alreadyInGraveyard));
        List<Permanent> destroyed = addAndDestroyCreatures(1);
        Card eligible = destroyed.getFirst().getCard();

        castContinue();
        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(eligible.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(alreadyInGraveyard.getId());
    }

    private List<Permanent> addAndDestroyCreatures(int count) {
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        }
        harness.inMutationScope(() -> creatures.forEach(permanent ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent)));
        return creatures;
    }

    private void castContinue() {
        harness.setHand(player1, List.of(new Continue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
