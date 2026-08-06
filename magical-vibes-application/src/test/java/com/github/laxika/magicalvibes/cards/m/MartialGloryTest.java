package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MartialGloryTest extends BaseCardTest {

    @Test
    @DisplayName("First target gets +3/+0 and second target gets +0/+3")
    void boostsBothTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MartialGlory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(firstId, secondId));
        harness.passBothPriorities();

        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(first.getPowerModifier()).isEqualTo(3);
        assertThat(first.getToughnessModifier()).isEqualTo(0);

        Permanent second = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(second.getPowerModifier()).isEqualTo(0);
        assertThat(second.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Both targets may be the same creature, which then gets +3/+3")
    void sameCreatureForBothTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MartialGlory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(bearId, bearId));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MartialGlory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(firstId, secondId));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(first.getPowerModifier()).isZero();
        assertThat(first.getToughnessModifier()).isZero();

        Permanent second = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(second.getPowerModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Second target still gets +0/+3 when first target is removed before resolution")
    void secondBoostAppliesWhenFirstTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MartialGlory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(firstId, secondId));

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(second.getPowerModifier()).isEqualTo(0);
        assertThat(second.getToughnessModifier()).isEqualTo(3);
    }
}
