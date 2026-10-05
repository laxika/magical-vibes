package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BindingMummy;
import com.github.laxika.magicalvibes.cards.p.PouncingCheetah;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InOketrasName.class, BindingMummy.class, PouncingCheetah.class})
class InOketrasNameTest extends BaseCardTest {

    @Test
    @DisplayName("Zombies get +2/+1 and other creatures get +1/+1")
    void boostsZombiesAndOtherCreatures() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new BindingMummy());
        Permanent cheetah = harness.addToBattlefieldAndReturn(player1, new PouncingCheetah());
        harness.setHand(player1, List.of(new InOketrasName()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(zombie.getPowerModifier()).isEqualTo(2);
        assertThat(zombie.getToughnessModifier()).isEqualTo(1);

        assertThat(cheetah.getPowerModifier()).isEqualTo(1);
        assertThat(cheetah.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new BindingMummy());
        harness.addToBattlefield(player2, new BindingMummy());
        harness.addToBattlefield(player2, new PouncingCheetah());
        harness.setHand(player1, List.of(new InOketrasName()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        for (Permanent p : gd.playerBattlefields.get(player2.getId())) {
            assertThat(p.getPowerModifier()).isEqualTo(0);
            assertThat(p.getToughnessModifier()).isEqualTo(0);
        }
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive either boost")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        harness.setHand(player1, List.of(new InOketrasName()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        Permanent zombie = harness.enterBattlefieldAndReturn(player1, new BindingMummy());
        Permanent cheetah = harness.enterBattlefieldAndReturn(player1, new PouncingCheetah());

        assertThat(zombie.getPowerModifier()).isZero();
        assertThat(zombie.getToughnessModifier()).isZero();
        assertThat(cheetah.getPowerModifier()).isZero();
        assertThat(cheetah.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Creatures entering while the spell is on the stack receive the boost")
    void boostsCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new InOketrasName()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);

        Permanent zombie = harness.enterBattlefieldAndReturn(player1, new BindingMummy());
        Permanent cheetah = harness.enterBattlefieldAndReturn(player1, new PouncingCheetah());
        harness.passBothPriorities();

        assertThat(zombie.getPowerModifier()).isEqualTo(2);
        assertThat(zombie.getToughnessModifier()).isEqualTo(1);
        assertThat(cheetah.getPowerModifier()).isEqualTo(1);
        assertThat(cheetah.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtCleanup() {
        harness.addToBattlefield(player1, new BindingMummy());
        harness.addToBattlefield(player1, new PouncingCheetah());
        harness.setHand(player1, List.of(new InOketrasName()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        for (Permanent p : gd.playerBattlefields.get(player1.getId())) {
            assertThat(p.getPowerModifier()).isEqualTo(0);
            assertThat(p.getToughnessModifier()).isEqualTo(0);
        }
    }
}
