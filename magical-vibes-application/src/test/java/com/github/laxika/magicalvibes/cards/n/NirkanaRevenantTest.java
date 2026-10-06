package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NirkanaRevenant.class, Swamp.class, Forest.class})
class NirkanaRevenantTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping your Swamp adds an additional black mana")
    void ownSwampProducesExtraBlack() {
        harness.addToBattlefield(player1, new NirkanaRevenant());
        harness.addToBattlefield(player1, new Swamp());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Swamp does not produce Nirkana Revenant's additional mana")
    void opponentSwampDoesNotProduceExtraBlack() {
        harness.addToBattlefield(player1, new NirkanaRevenant());
        harness.addToBattlefield(player2, new Swamp());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability gives +1/+1 until end of turn")
    void activatingBoostsUntilEndOfTurn() {
        Permanent revenant = addRevenantReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(revenant.getEffectivePower()).isEqualTo(6);
        assertThat(revenant.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(revenant.getEffectivePower()).isEqualTo(4);
        assertThat(revenant.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Tapping a non-Swamp land does not trigger the additional mana")
    void nonSwampDoesNotTrigger() {
        harness.addToBattlefield(player1, new NirkanaRevenant());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    private Permanent addRevenantReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NirkanaRevenant());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("Each Revenant adds one mana immediately, without using the stack")
    void multipleRevenantsAddManaImmediately() {
        harness.addToBattlefield(player1, new NirkanaRevenant());
        harness.addToBattlefield(player1, new NirkanaRevenant());
        harness.addToBattlefield(player2, new NirkanaRevenant());
        harness.addToBattlefield(player1, new Swamp());

        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost can be activated while tapped and summoning sick and affects only its source")
    void boostWorksWhileTappedAndSummoningSick() {
        Permanent revenant = harness.addToBattlefieldAndReturn(player1, new NirkanaRevenant());
        revenant.setSummoningSick(true);
        revenant.tap();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NirkanaRevenant());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(revenant.getEffectivePower()).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(revenant.getEffectivePower()).isEqualTo(5);
        assertThat(revenant.getEffectiveToughness()).isEqualTo(5);
        assertThat(other.getEffectivePower()).isEqualTo(4);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
    }
}
