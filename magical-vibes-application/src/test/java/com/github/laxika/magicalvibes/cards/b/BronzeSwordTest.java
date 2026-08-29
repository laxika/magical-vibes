package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BronzeSword.class, GrizzlyBears.class})
class BronzeSwordTest extends BaseCardTest {

    @Test
    void equippingBoostsTheTargetCreature() {
        Permanent sword = addReadySword(player1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    void boostDoesNotAffectOtherCreatures() {
        Permanent sword = addReadySword(player1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherBear)).isEqualTo(2);
        assertThat(sword.getAttachedTo()).isEqualTo(bear.getId());
    }

    @Test
    void boostEndsWhenSwordLeavesTheBattlefield() {
        Permanent sword = addReadySword(player1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, sword));

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    void equipCanOnlyTargetCreatureYouControl() {
        addReadySword(player1);
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void equipIsRestrictedToSorcerySpeed() {
        addReadySword(player1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadySword(Player player) {
        Permanent sword = harness.addToBattlefieldAndReturn(player, new BronzeSword());
        sword.setSummoningSick(false);
        return sword;
    }
}
