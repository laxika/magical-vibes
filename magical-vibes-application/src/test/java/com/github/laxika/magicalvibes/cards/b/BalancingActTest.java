package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalancingAct.class, DwarvenGrunt.class, Forest.class, Peek.class})
class BalancingActTest extends BaseCardTest {

    @Test
    @DisplayName("Balances all permanents together, regardless of type")
    void balancesAllPermanentsTogether() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new Forest());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new DwarvenGrunt());
        }
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        List<UUID> player1PermanentIds = gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getId)
                .toList();

        harness.castFromHand(player1, new BalancingAct(), "{2}{W}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, List.of(player1PermanentIds.get(0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Discards each hand down to the smallest hand size after balancing permanents")
    void balancesHandsDownToFewest() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new BalancingAct(), new DwarvenGrunt(), new Peek())));
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Lets the nonactive player choose excess permanents and discard excess cards")
    void letsNonactivePlayerChooseExcessPermanentsAndCards() {
        harness.setHand(player1, new ArrayList<>(List.of(new BalancingAct(), new Peek())));
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new DwarvenGrunt())));
        harness.addMana(player1, ManaColor.WHITE, 4);
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player1, new DwarvenGrunt());
        }
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new DwarvenGrunt());
        }

        List<UUID> player2PermanentIds = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId)
                .toList();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice permanentChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(permanentChoice).isNotNull();
        assertThat(permanentChoice.playerId()).isEqualTo(player2.getId());
        assertThat(permanentChoice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player2, List.of(player2PermanentIds.get(0)));

        PendingInteraction.DiscardChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.playerId()).isEqualTo(player2.getId());
        assertThat(discardChoice.remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
