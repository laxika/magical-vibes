package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aftershock.class, FightingDrake.class, Humility.class, Island.class, LotusPetal.class})
class AftershockTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target creature and deals 3 damage to its controller")
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FightingDrake());

        castAftershock(target);

        harness.assertInGraveyard(player2, "Fighting Drake");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Destroys the target artifact")
    void destroysTargetArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LotusPetal());

        castAftershock(target);

        harness.assertInGraveyard(player2, "Lotus Petal");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Destroys the target land")
    void destroysTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        castAftershock(target);

        harness.assertInGraveyard(player2, "Island");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Allows the target to regenerate")
    void allowsTargetToRegenerate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FightingDrake());
        target.setRegenerationShield(1);

        castAftershock(target);

        harness.assertOnBattlefield(player2, "Fighting Drake");
        assertThat(target.getRegenerationShield()).isZero();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Does not resolve when its target is illegal on resolution")
    void doesNotResolveWhenTargetIsIllegalOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FightingDrake());
        harness.setHand(player1, List.of(new Aftershock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Aftershock");
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Humility());
        harness.setHand(player1, List.of(new Aftershock()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    private void castAftershock(Permanent target) {
        harness.setHand(player1, List.of(new Aftershock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
    }
}
