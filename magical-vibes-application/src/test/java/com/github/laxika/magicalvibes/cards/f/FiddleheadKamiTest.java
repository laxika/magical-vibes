package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.m.MausoleumWanderer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FiddleheadKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell regenerates Fiddlehead Kami")
    void arcaneSpellRegeneratesThisCreature() {
        Permanent fiddleheadKami = addFiddleheadKami();
        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(fiddleheadKami.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a Spirit spell regenerates Fiddlehead Kami")
    void spiritSpellRegeneratesThisCreature() {
        Permanent fiddleheadKami = addFiddleheadKami();
        harness.setHand(player1, List.of(new MausoleumWanderer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(fiddleheadKami.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not regenerate Fiddlehead Kami")
    void unrelatedSpellDoesNotTrigger() {
        Permanent fiddleheadKami = addFiddleheadKami();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(fiddleheadKami.getRegenerationShield()).isZero();
    }

    private Permanent addFiddleheadKami() {
        return harness.addToBattlefieldAndReturn(player1, new FiddleheadKami());
    }
}
