package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RingwardenOwl;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FetidImp.class, RingwardenOwl.class})
class FetidImpTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants deathtouch until end of turn")
    void grantsDeathtouch() {
        Permanent imp = addCreatureReady(player1, new FetidImp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, imp, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off during the end-of-turn cleanup")
    void deathtouchWearsOff() {
        Permanent imp = addCreatureReady(player1, new FetidImp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, imp, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without black mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new FetidImp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A deathtouch-granted Fetid Imp kills a larger flying blocker")
    void deathtouchKillsLargerBlocker() {
        Permanent imp = addCreatureReady(player1, new FetidImp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new RingwardenOwl());

        imp.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("A tapped, summoning-sick Imp can activate on the opponent's turn and grants only itself deathtouch")
    void activatesWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new FetidImp());
        imp.setSummoningSick(true);
        imp.tap();
        Permanent otherImp = harness.addToBattlefieldAndReturn(player1, new FetidImp());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, imp, Keyword.DEATHTOUCH)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, imp, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherImp, Keyword.DEATHTOUCH)).isFalse();
        assertThat(imp.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colorless mana cannot pay for the black activation cost")
    void cannotActivateWithOnlyColorlessMana() {
        addCreatureReady(player1, new FetidImp());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }
}
