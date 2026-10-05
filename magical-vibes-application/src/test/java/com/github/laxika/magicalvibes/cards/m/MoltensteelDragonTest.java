package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Dismember;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltensteelDragon.class, Dismember.class})
class MoltensteelDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent perm = addDragonReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+0 until end of turn")
    void resolvingGivesPlusOnePlusZero() {
        Permanent perm = addDragonReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(1);
        assertThat(perm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate multiple times to stack the bonus")
    void canActivateMultipleTimes() {
        Permanent perm = addDragonReady(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(3);
        assertThat(perm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Bonus resets at end of turn cleanup")
    void bonusResetsAtEndOfTurn() {
        Permanent perm = addDragonReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(0);
        assertThat(perm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can pay Phyrexian mana with life instead of red mana")
    void canPayPhyrexianWithLife() {
        Permanent perm = addDragonReady(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(perm.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot pay two life when only one life remains")
    void cannotPayWithInsufficientLife() {
        Permanent perm = addDragonReady(player1);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        assertThat(gd.stack).isEmpty();
        assertThat(perm.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Red mana can pay the cost even with only one life remaining")
    void canPayWithRedManaAtOneLife() {
        Permanent perm = addDragonReady(player1);
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        assertThat(perm.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new MoltensteelDragon());
        perm.setSummoningSick(true);
        perm.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life is paid immediately but only the source is boosted on resolution")
    void boostsOnlySourceOnResolution() {
        Permanent source = addDragonReady(player1);
        Permanent ally = addDragonReady(player1);
        Permanent opponent = addDragonReady(player2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 18);
        assertThat(source.getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(ally.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Removing the source in response does not boost another Dragon")
    void sourceRemovedBeforeResolution() {
        Permanent source = addDragonReady(player1);
        Permanent ally = addDragonReady(player1);
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Dismember()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.assertInGraveyard(player1, "Moltensteel Dragon");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(ally.getPowerModifier()).isZero();
        harness.assertLife(player1, 18);
    }

    private Permanent addDragonReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MoltensteelDragon());
        perm.setSummoningSick(false);
        return perm;
    }
}
