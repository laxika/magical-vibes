package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RidingRedHareTest extends BaseCardTest {

    private Permanent addReadyCreature() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        perm.setSummoningSick(false);
        return perm;
    }

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new RidingRedHare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Resolving gives +3/+3 and horsemanship to the target creature")
    void resolvesBoostAndHorsemanship() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Boost and horsemanship wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.HORSEMANSHIP)).isFalse();
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addReadyCreature();
        castOn(target);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Riding Red Hare"));
    }
}
