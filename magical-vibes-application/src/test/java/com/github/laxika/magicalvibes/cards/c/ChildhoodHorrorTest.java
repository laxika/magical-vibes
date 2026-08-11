package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChildhoodHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats and can block before threshold")
    void baseStatsAndCanBlockBeforeThreshold() {
        Permanent attacker = addAttacker();
        Permanent horror = addHorror();

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(2);

        prepareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(horror),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(horror.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gets +2/+2 and cannot block with threshold")
    void thresholdBoostsAndPreventsBlocking() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent attacker = addAttacker();
        Permanent horror = addHorror();

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(4);

        prepareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(horror),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Loses the threshold bonus below seven cards")
    void losesThresholdBelowSevenCards() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent horror = addHorror();

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(4);

        gd.playerGraveyards.get(player2.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(2);
    }

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private Permanent addHorror() {
        Permanent horror = new Permanent(new ChildhoodHorror());
        horror.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(horror);
        return horror;
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private List<Card> graveyardWithSevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
