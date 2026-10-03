package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BorealElemental.class, GrizzlyBears.class, LightningBolt.class, ZuranSpellcaster.class})
class BorealElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's spell targeting Boreal Elemental costs {2} more")
    void opponentSpellTargetingBorealCostsMore() {
        Permanent boreal = harness.addToBattlefieldAndReturn(player1, new BorealElemental());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, boreal.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");
    }

    @Test
    @DisplayName("Boreal Elemental does not tax a spell targeting another permanent")
    void spellTargetingAnotherPermanentIsNotTaxed() {
        harness.addToBattlefield(player1, new BorealElemental());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Boreal Elemental does not tax its controller's spell")
    void ownSpellTargetingBorealIsNotTaxed() {
        Permanent boreal = harness.addToBattlefieldAndReturn(player1, new BorealElemental());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, boreal.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent's activated ability targeting Boreal Elemental is not taxed")
    void opponentActivatedAbilityTargetingBorealIsNotTaxed() {
        Permanent boreal = harness.addToBattlefieldAndReturn(player1, new BorealElemental());
        Permanent spellcaster = harness.addToBattlefieldAndReturn(player2, new ZuranSpellcaster());
        spellcaster.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, boreal.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent can pay the targeting tax with mana of other colors")
    void opponentPaysGenericTargetingTax() {
        Permanent boreal = harness.addToBattlefieldAndReturn(player1, new BorealElemental());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, boreal.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(boreal.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Boreal Elemental");
    }

    @Test
    @DisplayName("Another Boreal Elemental does not increase the tax on the targeted one")
    void multipleBorealsOnlyTaxTheirOwnTargets() {
        Permanent boreal = harness.addToBattlefieldAndReturn(player1, new BorealElemental());
        harness.addToBattlefield(player1, new BorealElemental());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 3);

        harness.castInstant(player2, 0, boreal.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Boreal Elemental does not tax spells targeting its controller")
    void spellTargetingControllerIsNotTaxed() {
        harness.addToBattlefield(player1, new BorealElemental());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    private void prepareOpponentCast(LightningBolt spell, ManaColor color, int amount) {
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, color, amount);
    }
}
