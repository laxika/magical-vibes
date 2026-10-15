package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AkkiUnderling;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiddleheadKami.class, SpiritualVisit.class, AkkiUnderling.class})
class FiddleheadKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell regenerates Fiddlehead Kami")
    void arcaneSpellRegeneratesThisCreature() {
        Permanent fiddleheadKami = addFiddleheadKami();
        harness.setHand(player1, List.of(new SpiritualVisit()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(fiddleheadKami.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a Spirit spell regenerates Fiddlehead Kami")
    void spiritSpellRegeneratesThisCreature() {
        Permanent fiddleheadKami = addFiddleheadKami();
        harness.setHand(player1, List.of(new FiddleheadKami()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(fiddleheadKami.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not regenerate Fiddlehead Kami")
    void unrelatedSpellDoesNotTrigger() {
        Permanent fiddleheadKami = addFiddleheadKami();
        harness.setHand(player1, List.of(new AkkiUnderling()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(fiddleheadKami.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("An opponent casting a Spirit or Arcane spell does not regenerate Fiddlehead Kami")
    void opponentSpellDoesNotTrigger() {
        Permanent fiddleheadKami = addFiddleheadKami();
        harness.setHand(player2, List.of(new SpiritualVisit()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0);

        assertThat(fiddleheadKami.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The regeneration trigger resolves before the Arcane spell and does not tap the creature")
    void regenerationShieldIsCreatedBeforeSpellResolves() {
        Permanent fiddleheadKami = addFiddleheadKami();
        harness.setHand(player1, List.of(new SpiritualVisit()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(fiddleheadKami.getRegenerationShield()).isZero();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(fiddleheadKami.getRegenerationShield()).isEqualTo(1);
        assertThat(fiddleheadKami.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(fiddleheadKami.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Fiddlehead Kami regenerates itself when its controller casts an Arcane spell")
    void multipleCopiesEachReceiveOneShield() {
        Permanent first = addFiddleheadKami();
        Permanent second = addFiddleheadKami();
        Permanent opponentsKami = harness.addToBattlefieldAndReturn(player2, new FiddleheadKami());
        harness.setHand(player1, List.of(new SpiritualVisit()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getRegenerationShield()).isEqualTo(1);
        assertThat(second.getRegenerationShield()).isEqualTo(1);
        assertThat(opponentsKami.getRegenerationShield()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Fiddlehead Kami does not trigger from casting itself")
    void castingItselfDoesNotCreateShield() {
        harness.setHand(player1, List.of(new FiddleheadKami()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fiddlehead Kami");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addFiddleheadKami() {
        return harness.addToBattlefieldAndReturn(player1, new FiddleheadKami());
    }
}
