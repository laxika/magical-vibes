package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Crusade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.Weakness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmrouKithkin.class, HillGiant.class, GrizzlyBears.class, Crusade.class, SavannahLions.class, Weakness.class})
class AmrouKithkinTest extends BaseCardTest {

    @Test
    @DisplayName("Amrou Kithkin can't be blocked by a creature with power 3 or greater")
    void cannotBeBlockedByPowerThreeOrGreater() {
        Permanent blockerPerm = addCreatureReady(player2, new HillGiant());

        Permanent atkPerm = addCreatureReady(player1, new AmrouKithkin());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Amrou Kithkin can be blocked by a creature with power less than 3")
    void canBeBlockedByLowPowerCreature() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm = addCreatureReady(player1, new AmrouKithkin());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    void cannotBeBlockedByEffectivePowerAtLeastThree() {
        harness.addToBattlefield(player2, new Crusade());
        Permanent blockerPerm = addCreatureReady(player2, new SavannahLions());

        Permanent atkPerm = addCreatureReady(player1, new AmrouKithkin());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedWhenPowerIsReducedBelowThree() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        addCreatureReady(player1, new AmrouKithkin());
        harness.setHand(player1, List.of(new Weakness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, blocker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockAnAttackerWithPowerThree() {
        Permanent blocker = addCreatureReady(player2, new AmrouKithkin());
        addCreatureReady(player1, new HillGiant());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
