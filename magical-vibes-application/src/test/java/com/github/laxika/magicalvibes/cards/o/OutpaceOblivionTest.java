package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutpaceOblivionTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 5 damage to up to one target creature")
    void etbDealsDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        castOutpace(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("ETB can resolve without a target")
    void etbCanResolveWithoutTarget() {
        harness.setHand(player1, List.of(new OutpaceOblivion()));
        addManaForOutpace();

        harness.castEnchantment(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof OutpaceOblivion);
    }

    @Test
    @DisplayName("Sacrifice ability damages only players below max speed")
    void sacrificeAbilityDamagesOnlyPlayersBelowMaxSpeed() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OutpaceOblivion());
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
    }

    @Test
    @DisplayName("ETB cannot target a land")
    void etbCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new OutpaceOblivion()));
        addManaForOutpace();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must");
    }

    private void castOutpace(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new OutpaceOblivion()));
        addManaForOutpace();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForOutpace() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
