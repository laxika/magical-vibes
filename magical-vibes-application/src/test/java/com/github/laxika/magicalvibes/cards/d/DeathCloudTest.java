package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Death Cloud")
class DeathCloudTest extends BaseCardTest {

    private List<UUID> permanentIds(Player player, CardType type, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(type))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private long permanentCount(Player player, CardType type) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(type))
                .count();
    }

    @Test
    @DisplayName("Each player loses X life")
    void eachPlayerLosesXLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new DeathCloud()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Resolves X discard and sacrifice steps for each player in order")
    void resolvesAllStepsForEachPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, new ArrayList<>(List.of(
                new DeathCloud(), new Peek(), new Forest(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Forest())));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new Forest());
        }

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player2,
                permanentIds(player2, CardType.CREATURE, 2));
        assertThat(permanentCount(player1, CardType.CREATURE)).isEqualTo(0);
        assertThat(permanentCount(player1, CardType.LAND)).isEqualTo(2);
        assertThat(permanentCount(player2, CardType.CREATURE)).isEqualTo(1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2,
                permanentIds(player2, CardType.LAND, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(permanentCount(player2, CardType.LAND)).isEqualTo(1);
    }
}
