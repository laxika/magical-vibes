package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GruulSpellbreaker.class, Shock.class})
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

    @Test
    @DisplayName("Hexproof allows self-targeting and ends immediately when Spellbreaker dies")
    void selfTargetingAndSourceRemoval() {
        Permanent spellbreaker = harness.addToBattlefieldAndReturn(player1, new GruulSpellbreaker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertLife(player1, 18);
        harness.castAndResolveInstant(player1, 0, spellbreaker.getId());
        harness.assertOnBattlefield(player1, "Gruul Spellbreaker");
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        harness.castAndResolveInstant(player1, 0, spellbreaker.getId());

        harness.assertInGraveyard(player1, "Gruul Spellbreaker");
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Riot haste persists through cleanup while turn-dependent hexproof expires")
    void riotHastePersistsAcrossTurnBoundary() {
        Permanent spellbreaker = castSpellbreaker(false);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, spellbreaker, Keyword.HASTE)).isTrue();
        assertThat(spellbreaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, spellbreaker, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
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
        return gqs.findPermanentById(gd, harness.getPermanentId(player1, "Gruul Spellbreaker"));
    }
}
