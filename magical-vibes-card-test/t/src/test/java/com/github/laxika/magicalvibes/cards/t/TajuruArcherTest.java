package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NimanaSellSword;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TajuruArcher.class, NimanaSellSword.class, SerraAngel.class, SuntailHawk.class, GrizzlyBears.class})
class TajuruArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may deal damage to a flying creature")
    void ownAllyEntryDealsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castArcher();
        resolveArcherTrigger(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage equals the number of Allies controlled when the ability resolves")
    void damageScalesWithAllyCount() {
        harness.addToBattlefield(player1, new NimanaSellSword());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castArcher();
        resolveArcherTrigger(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The Ally entry trigger may be declined")
    void mayBeDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        castArcher();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Tajuru Archer");
    }

    @Test
    @DisplayName("The trigger cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        castArcher();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An existing Archer and an entering Archer each trigger")
    void anotherAllyEntryTriggersEachArcher() {
        harness.addToBattlefield(player1, new TajuruArcher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castArcher();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("An Ally leaving before resolution reduces the damage")
    void countsAlliesAtResolution() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new NimanaSellSword());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castArcher();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ally);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opposing Allies are not counted")
    void excludesOpposingAlliesFromDamage() {
        harness.addToBattlefield(player2, new NimanaSellSword());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castArcher();
        resolveArcherTrigger(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Ally entering does not trigger the Archer")
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new TajuruArcher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The Archer can target a flying creature its controller controls")
    void canTargetOwnFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        castArcher();
        resolveArcherTrigger(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The Archer resolves normally when no flying creature can be targeted")
    void noLegalTargets() {
        castArcher();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tajuru Archer");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    @Test
    @DisplayName("An opponent's Ally entry does not trigger the Archer")
    void opposingAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new TajuruArcher());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new TajuruArcher(), "{2}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    private void castArcher() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TajuruArcher(), "{2}{G}");
    }

    private void resolveArcherTrigger(Permanent target) {
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
