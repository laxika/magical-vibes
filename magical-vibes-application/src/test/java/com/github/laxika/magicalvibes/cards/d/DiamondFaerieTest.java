package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiamondFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("Snow creatures you control get +1/+1")
    void boostsOwnSnowCreaturesOnly() {
        Permanent diamond = harness.addToBattlefieldAndReturn(player1, new DiamondFaerie());
        diamond.setSummoningSick(false);
        Permanent ownSnowCreature = addSnowCreature(player1);
        Permanent ownNonsnowCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentSnowCreature = addSnowCreature(player2);

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
    @DisplayName("The ability requires snow mana")
    void requiresSnowMana() {
        Permanent diamond = harness.addToBattlefieldAndReturn(player1, new DiamondFaerie());
        diamond.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, diamond), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }

    private Permanent addSnowCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        TestCards.mutableCard(creature).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        return creature;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
