package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NimanaSellSword;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private void castArcher() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TajuruArcher()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveArcherTrigger(Permanent target) {
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
