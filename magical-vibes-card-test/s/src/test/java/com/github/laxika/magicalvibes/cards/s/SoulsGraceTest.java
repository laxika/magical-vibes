package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulsGrace.class, CylianElf.class, Forest.class})
class SoulsGraceTest extends BaseCardTest {

    private void prepareSoulsGrace() {
        harness.setHand(player1, List.of(new SoulsGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Controller gains life equal to target creature's power")
    void gainsLifeEqualToPower() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new CylianElf()); // 2/2
        UUID targetId = harness.getPermanentId(player2, "Cylian Elf");

        prepareSoulsGrace();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life gain accounts for power modifiers")
    void gainsLifeAccountsForPowerModifiers() {
        harness.setLife(player1, 10);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        creature.setPowerModifier(3); // 2 + 3 = 5 effective power

        prepareSoulsGrace();
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new CylianElf()); // valid target so spell is playable
        UUID landId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();

        prepareSoulsGrace();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can target its controller's creature")
    void gainsLifeFromOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        prepareSoulsGrace();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Uses power at resolution rather than at casting")
    void usesPowerAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        prepareSoulsGrace();
        harness.castInstant(player1, 0, creature.getId());
        creature.setPowerModifier(3);

        harness.passBothPriorities();

        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Zero power causes no life gain")
    void zeroPowerGainsNoLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        creature.setPowerModifier(-2);
        prepareSoulsGrace();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertLife(player1, 20);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Negative power causes no life gain or life loss")
    void negativePowerDoesNotLoseLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        creature.setPowerModifier(-3);
        prepareSoulsGrace();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertLife(player1, 20);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Gains no life when the target leaves before resolution")
    void removedTargetGainsNoLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        prepareSoulsGrace();
        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SoulsGrace);
    }
}
