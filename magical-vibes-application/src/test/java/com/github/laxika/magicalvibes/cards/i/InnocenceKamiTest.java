package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InnocenceKamiTest extends BaseCardTest {

    @Test
    @DisplayName("White mana and tapping Innocence Kami taps target creature")
    void tapsTargetCreature() {
        Permanent kami = addReadyKami(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(kami.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting an Arcane spell untaps Innocence Kami")
    void arcaneSpellUntapsKami() {
        Permanent kami = addReadyKami(player1);
        kami.tap();
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(kami.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a Spirit spell untaps Innocence Kami")
    void spiritSpellUntapsKami() {
        Permanent kami = addReadyKami(player1);
        kami.tap();
        harness.setHand(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(kami.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not untap Innocence Kami")
    void unrelatedSpellDoesNotUntapKami() {
        Permanent kami = addReadyKami(player1);
        kami.tap();
        harness.setHand(player1, List.of(new DevotedRetainer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(kami.isTapped()).isTrue();
    }

    private Permanent addReadyKami(Player player) {
        return addCreatureReady(player, new InnocenceKami());
    }
}
