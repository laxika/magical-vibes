package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HideousTaskmaster.class, GrizzlyBears.class})
class HideousTaskmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps, and grants trample, haste, and annihilator 1")
    void castTriggerAppliesTemporaryAbilities() {
        Permanent target = addCreatureReady(player2);
        target.tap();

        castHideousTaskmaster(List.of(target.getId()));

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("The stolen creature makes the defending player sacrifice one permanent when it attacks")
    void stolenCreatureHasAnnihilatorOne() {
        Permanent target = addCreatureReady(player2);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castHideousTaskmaster(List.of(target.getId()));

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        declareAttackers(player1, List.of(attackerIndex));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Control and temporary abilities expire at cleanup")
    void temporaryEffectsExpireAtCleanup() {
        Permanent target = addCreatureReady(player2);

        castHideousTaskmaster(List.of(target.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Allows choosing no creatures")
    void canChooseNoTargets() {
        castHideousTaskmaster(List.of());

        harness.assertOnBattlefield(player1, "Hideous Taskmaster");
    }

    @Test
    @DisplayName("Cannot choose two creatures controlled by the same opponent")
    void cannotChooseTwoCreaturesOfSameOpponent() {
        Permanent first = addCreatureReady(player2);
        Permanent second = addCreatureReady(player2);
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void castHideousTaskmaster(List<UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        if (!targetIds.isEmpty()) {
            harness.handlePermanentChosen(player1, targetIds.getFirst());
            if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
                harness.handlePermanentChosen(player1, player1.getId());
            }
        }
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new HideousTaskmaster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
