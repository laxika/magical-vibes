package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PromiseOfLoyalty.class, GrizzlyBears.class})
class PromiseOfLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Each player keeps one creature, which gets a vow counter, and sacrifices the rest")
    void keepsOneCreaturePerPlayerAndSacrificesTheRest() {
        Permanent ownKept = addCreature(player1);
        Permanent ownSacrificed = addCreature(player1);
        Permanent opposingKept = addCreature(player2);
        Permanent opposingSacrificed = addCreature(player2);

        castPromise();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class))
                .isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownKept.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opposingKept.getId()));

        assertThat(isOnBattlefield(player1, ownKept)).isTrue();
        assertThat(isOnBattlefield(player2, opposingKept)).isTrue();
        assertThat(isOnBattlefield(player1, ownSacrificed)).isFalse();
        assertThat(isOnBattlefield(player2, opposingSacrificed)).isFalse();
        assertThat(ownKept.getCounterCount(CounterType.VOW)).isEqualTo(1);
        assertThat(opposingKept.getCounterCount(CounterType.VOW)).isEqualTo(1);
    }

    @Test
    @DisplayName("A chosen creature with a vow counter cannot attack the spell's controller")
    void chosenCreatureCannotAttackSpellController() {
        Permanent ownChoice = addCreature(player1);
        addCreature(player1);
        Permanent chosen = addCreature(player2);
        addCreature(player2);
        castPromise();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownChoice.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(chosen);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2,
                List.of(attackerIndex), Map.of(attackerIndex, player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void castPromise() {
        harness.setHand(player1, List.of(new PromiseOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private boolean isOnBattlefield(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(candidate -> candidate.getId().equals(permanent.getId()));
    }
}
