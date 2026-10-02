package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheBalance.class, Forest.class, GrizzlyBears.class})
class MagusOfTheBalanceTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and balances lands, hands, and creatures")
    void balancesEachCategory() {
        addCreatureReady(player1, new MagusOfTheBalance());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice landChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(landChoice).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, matchingPermanentIds(player1, CardType.LAND, 2));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.MultiPermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(creatureChoice).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, matchingPermanentIds(player1, CardType.CREATURE, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(countMatching(player1, CardType.LAND)).isEqualTo(1);
        assertThat(countMatching(player2, CardType.LAND)).isEqualTo(1);
        assertThat(countMatching(player1, CardType.CREATURE)).isEqualTo(1);
        assertThat(countMatching(player2, CardType.CREATURE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Magus of the Balance");
    }

    private List<UUID> matchingPermanentIds(Player player, CardType type, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(type))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private long countMatching(Player player, CardType type) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(type))
                .count();
    }
}
