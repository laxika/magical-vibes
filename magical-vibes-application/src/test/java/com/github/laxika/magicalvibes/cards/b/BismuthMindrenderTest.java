package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BismuthMindrender.class, Forest.class, GrizzlyBears.class})
class BismuthMindrenderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles through lands and grants a life-paid cast")
    void combatDamageDigsToNonlandAndGrantsLifeCastPermission() {
        addAttackingMindrender();
        Card land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, nonland));

        resolveCombatAndTrigger();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(land.getId(), nonland.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(nonland.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(nonland.getId());
        assertThat(gd.exilePlayForLifeEqualToManaValue).contains(nonland.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, nonland.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().equals(nonland));
        assertThat(gd.findExiledCard(nonland.getId())).isNull();
    }

    @Test
    @DisplayName("Exiling only lands grants no cast permission")
    void onlyLandsDoNotGrantPermission() {
        addAttackingMindrender();
        Forest land = new Forest();
        harness.setLibrary(player2, List.of(land));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addAttackingMindrender() {
        Permanent mindrender = addCreatureReady(player1, new BismuthMindrender());
        mindrender.setAttacking(true);
        return mindrender;
    }

    private void resolveCombatAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
