package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AkroanConscriptorTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic gains control of another creature, untaps it, and grants haste")
    void heroicGainsControlUntapsAndHastesAnotherCreature() {
        harness.addToBattlefield(player1, new AkroanConscriptor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Akroan Conscriptor"));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Heroic cannot target Akroan Conscriptor itself")
    void heroicCannotTargetItself() {
        Permanent conscriptor = harness.addToBattlefieldAndReturn(player1, new AkroanConscriptor());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, conscriptor.getId());
        harness.passBothPriorities();

        assertThat(conscriptor.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger heroic")
    void targetingPlayerDoesNotTriggerHeroic() {
        Permanent conscriptor = harness.addToBattlefieldAndReturn(player1, new AkroanConscriptor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(conscriptor.hasKeyword(Keyword.HASTE)).isFalse();
    }
}
