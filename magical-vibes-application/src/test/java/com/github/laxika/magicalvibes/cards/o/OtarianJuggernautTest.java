package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
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

class OtarianJuggernautTest extends BaseCardTest {

    @Test
    @DisplayName("Otarian Juggernaut gets +3/+0 at threshold")
    void getsThresholdBonus() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent juggernaut = addReadyJuggernaut();

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(3);
    }

    @Test
    @DisplayName("Otarian Juggernaut has no threshold bonus below seven cards")
    void hasNoThresholdBonusBelowSevenCards() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent juggernaut = addReadyJuggernaut();

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(3);
    }

    @Test
    @DisplayName("Otarian Juggernaut must attack only while threshold is met")
    void mustAttackAtThresholdOnly() {
        Permanent juggernaut = addReadyJuggernaut();
        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of());
        assertThat(juggernaut.isAttacking()).isFalse();

        harness.setGraveyard(player1, graveyardCards(7));
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Otarian Juggernaut cannot be blocked by a Wall")
    void cannotBeBlockedByWall() {
        Permanent juggernaut = addReadyJuggernaut();
        juggernaut.setAttacking(true);

        Permanent wall = new Permanent(new WallOfWood());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wall);

        beginDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(wall);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(juggernaut);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Otarian Juggernaut can be blocked by a non-Wall creature")
    void canBeBlockedByNonWall() {
        Permanent juggernaut = addReadyJuggernaut();
        juggernaut.setAttacking(true);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        beginDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(juggernaut);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(bears.isBlocking()).isTrue();
    }

    private Permanent addReadyJuggernaut() {
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new OtarianJuggernaut());
        juggernaut.setSummoningSick(false);
        return juggernaut;
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void beginDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }
}
