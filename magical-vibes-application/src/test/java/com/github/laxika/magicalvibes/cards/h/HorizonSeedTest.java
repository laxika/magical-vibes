package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HorizonSeedTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell gives a target creature a regeneration shield")
    void arcaneSpellRegeneratesTargetCreature() {
        harness.addToBattlefield(player1, new HorizonSeed());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a Spirit spell gives a target creature a regeneration shield")
    void spiritSpellRegeneratesTargetCreature() {
        harness.addToBattlefield(player1, new HorizonSeed());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new HorizonSeed());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isZero();
    }
}
