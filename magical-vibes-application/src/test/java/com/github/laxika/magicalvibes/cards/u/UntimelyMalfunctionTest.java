package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UntimelyMalfunction.class, Boomerang.class, FountainOfYouth.class, GrizzlyBears.class})
class UntimelyMalfunctionTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        cast(new int[]{0}, List.of(harness.getPermanentId(player2, "Fountain of Youth")));

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void retargetsSingleTargetSpellOrAbility() {
        Permanent originalTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent newTarget = addCreatureReady(player2, new GrizzlyBears());
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new UntimelyMalfunction()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, 1, new int[]{1}, boomerang.getId(), List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, newTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(originalTarget.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(newTarget.getId()));
    }

    @Test
    void makesOneOrTwoCreaturesUnableToBlock() {
        Permanent creature1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player2, new GrizzlyBears());

        cast(new int[]{2}, List.of(creature1.getId(), creature2.getId()));

        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void blockRestrictionRejectsNonCreatureTarget() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        assertThatThrownBy(() -> cast(new int[]{2},
                List.of(harness.getPermanentId(player2, "Fountain of Youth"))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new UntimelyMalfunction()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 1, modes, targetIds);
        harness.passBothPriorities();
    }
}
