package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plummet;
import com.github.laxika.magicalvibes.cards.z.ZuranEnchanter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirElemental.class, GrizzlyBears.class, HighTroller.class, Plummet.class,
        ZuranEnchanter.class})
class HighTrollerTest extends BaseCardTest {

    @Test
    void reducesTargetedSpellAndRandomizesItsTarget() {
        harness.addToBattlefield(player1, new HighTroller());
        var originalTarget = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        var alternateTarget = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player2, List.of(new Plummet()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, originalTarget.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getId().equals(originalTarget.getId()));
        harness.passBothPriorities();

        StackEntry plummet = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Plummet"))
                .findFirst()
                .orElseThrow();
        assertThat(plummet.getTargetId()).isEqualTo(alternateTarget.getId());
    }

    @Test
    void reducesTargetedActivatedAbilityForAnyPlayer() {
        harness.addToBattlefield(player1, new HighTroller());
        var enchanter = harness.addToBattlefieldAndReturn(player2, new ZuranEnchanter());
        enchanter.setSummoningSick(false);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceNonTargetedSpells() {
        harness.addToBattlefield(player1, new HighTroller());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
