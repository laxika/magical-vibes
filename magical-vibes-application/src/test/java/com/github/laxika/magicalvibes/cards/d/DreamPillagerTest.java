package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamPillager.class, Shock.class, Forest.class})
class DreamPillagerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles that many cards and grants permission for nonlands")
    void combatDamageExilesDamageAmountAndAllowsNonlands() {
        addAttackingPillager(player1);
        Card firstSpell = new Shock();
        Card firstLand = new Forest();
        Card secondSpell = new Shock();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstSpell, firstLand, secondSpell, secondLand));

        resolveCombatAndTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(firstSpell, firstLand, secondSpell, secondLand);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(firstSpell.getId(), player1.getId())
                .containsEntry(secondSpell.getId(), player1.getId())
                .doesNotContainKeys(firstLand.getId(), secondLand.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost)
                .doesNotContain(firstSpell.getId(), secondSpell.getId());
    }

    @Test
    @DisplayName("A nonland exiled this way can be cast for its normal cost")
    void castsExiledSpellForNormalCost() {
        addAttackingPillager(player1);
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock, new Forest(), new Forest(), new Forest()));

        resolveCombatAndTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The cast permission expires at end of turn")
    void castPermissionExpiresAtEndOfTurn() {
        addAttackingPillager(player1);
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock, new Forest(), new Forest(), new Forest()));

        resolveCombatAndTrigger();
        assertThat(gd.exilePlayPermissions).containsKey(shock.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(shock.getId());
    }

    private Permanent addAttackingPillager(Player player) {
        Permanent pillager = addCreatureReady(player, new DreamPillager());
        pillager.setAttacking(true);
        return pillager;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
