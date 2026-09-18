package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GempalmIncinerator;
import com.github.laxika.magicalvibes.cards.g.GoblinTurncoat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfHope.class, GempalmIncinerator.class, GoblinTurncoat.class})
class WallOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains life equal to noncombat damage dealt to Wall of Hope")
    void gainsLifeFromNoncombatDamage() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfHope());
        addCreatureReady(player1, new GoblinTurncoat());
        addCreatureReady(player1, new GoblinTurncoat());
        harness.setLife(player2, 10);

        cycleGempalmAndAcceptDamage(wall);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller gains life even when damage is lethal to Wall of Hope")
    void gainsLifeFromLethalDamage() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfHope());
        addCreatureReady(player1, new GoblinTurncoat());
        addCreatureReady(player1, new GoblinTurncoat());
        addCreatureReady(player1, new GoblinTurncoat());
        harness.setLife(player2, 10);

        cycleGempalmAndAcceptDamage(wall);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        harness.assertInGraveyard(player2, "Wall of Hope");
    }

    @Test
    @DisplayName("Controller gains life equal to combat damage dealt to Wall of Hope")
    void gainsLifeFromCombatDamage() {
        Permanent wall = addCreatureReady(player2, new WallOfHope());
        Permanent attacker = addCreatureReady(player1, new GoblinTurncoat());

        attacker.setAttacking(true);

        wall.setBlocking(true);
        wall.addBlockingTarget(0);

        harness.setLife(player2, 10);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }

    private void cycleGempalmAndAcceptDamage(Permanent target) {
        harness.setHand(player1, List.of(new GempalmIncinerator()));
        harness.setLibrary(player1, List.of(new GoblinTurncoat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
    }
}
