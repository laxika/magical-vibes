package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GruulSpellbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Riot can put a +1/+1 counter on Gruul Spellbreaker")
    void riotAddsCounter() {
        Permanent spellbreaker = castSpellbreaker(true);

        assertThat(spellbreaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, spellbreaker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spellbreaker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, spellbreaker, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Riot can give Gruul Spellbreaker persistent haste")
    void riotAddsHaste() {
        Permanent spellbreaker = castSpellbreaker(false);

        assertThat(gqs.hasKeyword(gd, spellbreaker, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("During its controller's turn, Gruul Spellbreaker and its controller have hexproof")
    void hasHexproofDuringControllerTurn() {
        Permanent spellbreaker = harness.addToBattlefieldAndReturn(player1, new GruulSpellbreaker());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, spellbreaker, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Hexproof ends when the active player is an opponent")
    void losesHexproofOnOpponentTurn() {
        Permanent spellbreaker = harness.addToBattlefieldAndReturn(player1, new GruulSpellbreaker());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, spellbreaker, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("An opponent cannot target Spellbreaker or its controller during their turn")
    void opponentCannotTargetSpellbreakerOrController() {
        Permanent spellbreaker = harness.addToBattlefieldAndReturn(player1, new GruulSpellbreaker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, spellbreaker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("An opponent can target Spellbreaker and its controller on the opponent's turn")
    void opponentCanTargetAfterTurnChanges() {
        Permanent spellbreaker = harness.addToBattlefieldAndReturn(player1, new GruulSpellbreaker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, spellbreaker.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent castSpellbreaker(boolean chooseCounter) {
        harness.setHand(player1, List.of(new GruulSpellbreaker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, chooseCounter);
        }
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GruulSpellbreaker)
                .findFirst()
                .orElseThrow();
    }
}
