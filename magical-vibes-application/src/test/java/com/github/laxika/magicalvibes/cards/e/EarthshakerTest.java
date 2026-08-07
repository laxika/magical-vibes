package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EarthshakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell deals 2 damage to each creature without flying")
    void arcaneSpellDamagesGroundCreatures() {
        addCreatureReady(player1, new Earthshaker());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent flyer = addCreatureReady(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(flyer.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Casting a Spirit spell triggers the damage")
    void spiritSpellDamagesGroundCreatures() {
        addCreatureReady(player1, new Earthshaker());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger the damage")
    void unrelatedSpellDoesNotTrigger() {
        addCreatureReady(player1, new Earthshaker());
        Permanent ground = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevotedRetainer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(ground.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Earthshaker damages itself since it has no flying")
    void earthshakerDamagesItself() {
        Permanent earthshaker = addCreatureReady(player1, new Earthshaker());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(earthshaker.getMarkedDamage()).isEqualTo(2);
    }
}
