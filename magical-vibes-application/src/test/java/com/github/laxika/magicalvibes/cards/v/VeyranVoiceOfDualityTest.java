package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WitherbloomApprentice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeyranVoiceOfDuality.class, WitherbloomApprentice.class,
        BarkshellBlessing.class, Shock.class, GrizzlyBears.class})
class VeyranVoiceOfDualityTest extends BaseCardTest {

    @Test
    @DisplayName("Veyran doubles its own magecraft trigger")
    void doublesItsOwnMagecraftTrigger() {
        Permanent veyran = addCreatureReady(player1, new VeyranVoiceOfDuality());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        assertThat(veyran.getEffectivePower()).isEqualTo(4);
        assertThat(veyran.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Veyran doubles another permanent's magecraft trigger")
    void doublesAnotherPermanentMagecraftTrigger() {
        Permanent veyran = addCreatureReady(player1, new VeyranVoiceOfDuality());
        addCreatureReady(player1, new WitherbloomApprentice());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveStack();

        assertThat(veyran.getEffectivePower()).isEqualTo(4);
        assertThat(veyran.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Veyran doubles magecraft from a copied instant")
    void doublesMagecraftFromCopiedInstant() {
        Permanent veyran = addCreatureReady(player1, new VeyranVoiceOfDuality());
        addCreatureReady(player1, new WitherbloomApprentice());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveStack();

        assertThat(veyran.getEffectivePower()).isEqualTo(6);
        assertThat(veyran.getEffectiveToughness()).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }
}
