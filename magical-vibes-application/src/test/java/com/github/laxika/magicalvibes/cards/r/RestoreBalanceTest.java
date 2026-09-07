package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RestoreBalance.class, Forest.class, GrizzlyBears.class})
class RestoreBalanceTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Restore Balance with six time counters")
    void suspendExilesWithSixTimeCounters() {
        RestoreBalance card = new RestoreBalance();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The suspended card free-casts and balances lands, hands, and creatures")
    void freeCastBalancesAllCategories() {
        RestoreBalance card = new RestoreBalance();
        harness.setHand(player1, new ArrayList<>(List.of(card, new GrizzlyBears(), new Forest())));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        List<Permanent> player1Lands = addForests(player1, 3);
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateHandAbility(player1, 0, null);
        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice landChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(landChoice).isNotNull();
        assertThat(landChoice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, player1Lands.subList(0, 2).stream()
                .map(Permanent::getId).toList());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countCreatures(player1)).isZero();
    }

    private List<Permanent> addForests(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Permanent> lands = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            lands.add(harness.addToBattlefieldAndReturn(player, new Forest()));
        }
        return lands;
    }

    private long landCount(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .count();
    }

    private long countCreatures(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .count();
    }

}
