package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DelugeVirtuosoTest extends BaseCardTest {

    private Permanent addReadyVirtuoso(Player player) {
        Permanent perm = new Permanent(new DelugeVirtuoso());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    // ===== ETB: tap + stun (skip untap) =====

    @Test
    @DisplayName("ETB taps target opponent creature and puts a stun counter (skip-untap) on it")
    void etbTapsAndStunsTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bears.isTapped()).isFalse();
        UUID targetId = bears.getId();

        harness.setHand(player1, List.of(new DelugeVirtuoso()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    // ===== Opus boost =====

    @Test
    @DisplayName("Casting a one-mana instant gives +1/+1")
    void cheapSpellGivesPlusOne() {
        Permanent virtuoso = addReadyVirtuoso(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(virtuoso.getPowerModifier()).isEqualTo(1);
        assertThat(virtuoso.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a four-mana spell gives +1/+1 (below threshold)")
    void fourManaSpellGivesPlusOne() {
        Permanent virtuoso = addReadyVirtuoso(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(virtuoso.getPowerModifier()).isEqualTo(1);
        assertThat(virtuoso.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a five-mana spell gives +2/+2 instead")
    void fiveManaSpellGivesPlusTwo() {
        Permanent virtuoso = addReadyVirtuoso(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(virtuoso.getPowerModifier()).isEqualTo(2);
        assertThat(virtuoso.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the Opus boost")
    void creatureSpellDoesNotTrigger() {
        Permanent virtuoso = addReadyVirtuoso(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(virtuoso.getPowerModifier()).isZero();
        assertThat(virtuoso.getToughnessModifier()).isZero();
    }
}
