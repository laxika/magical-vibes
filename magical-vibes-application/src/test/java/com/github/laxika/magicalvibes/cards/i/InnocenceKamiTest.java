package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.cards.h.HonorWornShaku;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InnocenceKami.class, DesperateRitual.class, DevotedRetainer.class,
        HarshDeceiver.class, HonorWornShaku.class})
class InnocenceKamiTest extends BaseCardTest {

    @Test
    @DisplayName("White mana and tapping Innocence Kami taps target creature")
    void tapsTargetCreature() {
        Permanent kami = addReadyKami(player1);
        Permanent target = addCreatureReady(player2, new DevotedRetainer());
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

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(kami.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a Spirit spell untaps Innocence Kami")
    void spiritSpellUntapsKami() {
        Permanent kami = addReadyKami(player1);
        kami.tap();

        harness.castFromHand(player1, new HarshDeceiver(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(kami.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not untap Innocence Kami")
    void unrelatedSpellDoesNotUntapKami() {
        Permanent kami = addReadyKami(player1);
        kami.tap();

        harness.castFromHand(player1, new DevotedRetainer(), "{W}");
        harness.passBothPriorities();

        assertThat(kami.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Spirit spell cast by an opponent does not untap Innocence Kami")
    void opponentSpiritSpellDoesNotUntapKami() {
        Permanent kami = addReadyKami(player1);
        kami.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new HarshDeceiver(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(kami.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Innocence Kami cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyKami(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HonorWornShaku());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKami(Player player) {
        return addCreatureReady(player, new InnocenceKami());
    }
}
