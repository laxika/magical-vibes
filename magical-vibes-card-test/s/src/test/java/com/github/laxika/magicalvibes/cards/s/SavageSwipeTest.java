package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavageSwipe.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class SavageSwipeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a 2-power creature before it fights")
    void boostsTwoPowerCreatureBeforeFight() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castSwipe(ownCreature, opponent);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Does not boost a creature whose power is not 2")
    void doesNotBoostCreatureWithDifferentPower() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSwipe(ownCreature, opponent);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The boost lasts until end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castSwipe(ownCreature, opponent);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Requires a creature you control and a creature an opponent controls")
    void rejectsIllegalTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherOwnCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavageSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponent.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ownCreature.getId(), otherOwnCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Checks power when resolving rather than when cast")
    void checksPowerAtResolution() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SavageSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(ownCreature.getId(), opponent.getId()));

        ownCreature.setPowerModifier(1);
        ownCreature.setToughnessModifier(1);
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("A creature below 2 power still fights without a boost")
    void onePowerCreatureStillFights() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castSwipe(ownCreature, opponent);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Still boosts the legal first target when the opposing target leaves")
    void boostsWhenOpponentLeavesBeforeResolution() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SavageSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(ownCreature.getId(), opponent.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Neither creature fights when the first target changes controller")
    void noFightWhenFirstTargetChangesController() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SavageSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(ownCreature.getId(), opponent.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        gd.playerBattlefields.get(player2.getId()).add(ownCreature);
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponent.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    private void castSwipe(Permanent ownCreature, Permanent opponent) {
        harness.setHand(player1, List.of(new SavageSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(ownCreature.getId(), opponent.getId()));
    }
}
