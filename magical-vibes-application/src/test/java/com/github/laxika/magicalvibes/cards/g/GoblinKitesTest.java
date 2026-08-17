package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinKitesTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a legal target flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent kites = new Permanent(new GoblinKites());
        Permanent wizard = new Permanent(new FugitiveWizard());
        gd.playerBattlefields.get(player1.getId()).add(kites);
        gd.playerBattlefields.get(player1.getId()).add(wizard);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, wizard.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flips at the next end step and sacrifices only on a lost flip")
    void flipsAndMaySacrificeAtNextEndStep() {
        Permanent kites = new Permanent(new GoblinKites());
        Permanent wizard = new Permanent(new FugitiveWizard());
        gd.playerBattlefields.get(player1.getId()).add(kites);
        gd.playerBattlefields.get(player1.getId()).add(wizard);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, wizard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        boolean lostFlip = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("loses the coin flip for Goblin Kites"));
        boolean onBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getId().equals(wizard.getId()));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Goblin Kites"));
        assertThat(onBattlefield).isEqualTo(!lostFlip);
    }

    @Test
    @DisplayName("Rejects creatures with toughness above 2 and noncreatures")
    void rejectsIllegalTargets() {
        Permanent kites = new Permanent(new GoblinKites());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setToughnessModifier(1);
        Permanent mountain = new Permanent(new Mountain());
        gd.playerBattlefields.get(player1.getId()).add(kites);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.playerBattlefields.get(player1.getId()).add(mountain);

        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
