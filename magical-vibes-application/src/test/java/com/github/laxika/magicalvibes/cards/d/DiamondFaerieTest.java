package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.r.RonomUnicorn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiamondFaerie.class, BorealCentaur.class, RonomUnicorn.class})
class DiamondFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("Snow creatures you control get +1/+1")
    void boostsOwnSnowCreaturesOnly() {
        Permanent diamond = addCreatureReady(player1, new DiamondFaerie());
        Permanent ownSnowCreature = addCreatureReady(player1, new BorealCentaur());
        Permanent ownNonsnowCreature = addCreatureReady(player1, new RonomUnicorn());
        Permanent opponentSnowCreature = addCreatureReady(player2, new BorealCentaur());

        addAbilityMana();
        harness.activateAbility(player1, indexOf(player1, diamond), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, diamond)).isEqualTo(diamond.getCard().getPower() + 1);
        assertThat(gqs.getEffectiveToughness(gd, diamond)).isEqualTo(diamond.getCard().getToughness() + 1);
        assertThat(gqs.getEffectivePower(gd, ownSnowCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownSnowCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownNonsnowCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNonsnowCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSnowCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSnowCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent diamond = addCreatureReady(player1, new DiamondFaerie());
        Permanent ownSnowCreature = addCreatureReady(player1, new BorealCentaur());

        addAbilityMana();
        harness.activateAbility(player1, indexOf(player1, diamond), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, diamond)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownSnowCreature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, diamond)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, diamond)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownSnowCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSnowCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability requires snow mana")
    void requiresSnowMana() {
        Permanent diamond = addCreatureReady(player1, new DiamondFaerie());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, diamond), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
