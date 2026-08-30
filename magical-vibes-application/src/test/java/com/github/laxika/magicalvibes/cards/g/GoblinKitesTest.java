package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HomaridWarrior;
import com.github.laxika.magicalvibes.cards.i.IcatianPriest;
import com.github.laxika.magicalvibes.cards.i.IcatianLieutenant;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
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

@CardUsed({GoblinKites.class, RiverMerfolk.class, HomaridWarrior.class, IcatianPriest.class, IcatianLieutenant.class})
class GoblinKitesTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a legal target flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new GoblinKites());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flips at the next end step and sacrifices only on a lost flip")
    void flipsAndMaySacrificeAtNextEndStep() {
        harness.addToBattlefieldAndReturn(player1, new GoblinKites());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        boolean lostFlip = gameLogContains("loses the coin flip for Goblin Kites");
        assertThat(gameLogContains("coin flip for Goblin Kites")).isTrue();
        if (lostFlip) {
            harness.assertNotOnBattlefield(player1, "River Merfolk");
        } else {
            harness.assertOnBattlefield(player1, "River Merfolk");
        }
    }

    @Test
    @DisplayName("Rejects creatures with toughness above 2, opposing creatures, and noncreatures")
    void rejectsIllegalTargets() {
        Permanent kites = harness.addToBattlefieldAndReturn(player1, new GoblinKites());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new HomaridWarrior());
        Permanent opponentMerfolk = harness.addToBattlefieldAndReturn(player2, new RiverMerfolk());

        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, warrior.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentMerfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kites.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rechecks the toughness restriction when the ability resolves")
    void fizzlesIfTargetBecomesTooToughBeforeResolution() {
        harness.addToBattlefieldAndReturn(player1, new GoblinKites());
        harness.addToBattlefieldAndReturn(player1, new IcatianPriest());
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new IcatianLieutenant());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, lieutenant.getId());
        harness.activateAbility(player1, 1, null, lieutenant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, lieutenant)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lieutenant, Keyword.FLYING)).isFalse();
        assertThat(gameLogContains("coin flip for Goblin Kites")).isFalse();
    }
}
