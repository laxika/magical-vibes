package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChildhoodHorror.class, DuskImp.class})
class ChildhoodHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats and can block before threshold")
    void baseStatsAndCanBlockBeforeThreshold() {
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        attacker.setAttacking(true);
        Permanent horror = addCreatureReady(player2, new ChildhoodHorror());

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(2);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(horror),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(horror.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gets +2/+2 and cannot block with threshold")
    void thresholdBoostsAndPreventsBlocking() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        attacker.setAttacking(true);
        Permanent horror = addCreatureReady(player2, new ChildhoodHorror());

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(4);

        prepareDeclareBlockers(player1);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(horror),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        attacker.setAttacking(true);
        Permanent horror = addCreatureReady(player2, new ChildhoodHorror());

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(2);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(horror),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(horror.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Loses the threshold bonus below seven cards")
    void losesThresholdBelowSevenCards() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent horror = addCreatureReady(player2, new ChildhoodHorror());

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(4);

        gd.playerGraveyards.get(player2.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(2);
    }

    private List<Card> graveyardWithSevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new DuskImp());
        }
        return cards;
    }
}
