package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KitsaOtterballElite;
import com.github.laxika.magicalvibes.cards.p.PestilenceRats;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValleyFloodcaller.class, Frogmite.class, GrizzlyBears.class, KitsaOtterballElite.class,
        PestilenceRats.class, Shock.class, ZephyrFalcon.class})
class ValleyFloodcallerTest extends BaseCardTest {

    @Test
    void grantsFlashToNoncreatureSpells() {
        harness.addToBattlefield(player1, new ValleyFloodcaller());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Shock"));
    }

    @Test
    void doesNotGrantFlashToCreatureSpells() {
        harness.addToBattlefield(player1, new ValleyFloodcaller());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void buffsAndUntapsControlledBirdsFrogsOttersAndRatsOnly() {
        Permanent floodcaller = addCreatureReady(player1, new ValleyFloodcaller());
        Permanent bird = addCreatureReady(player1, new ZephyrFalcon());
        Permanent frog = addCreatureReady(player1, new Frogmite());
        Permanent otter = addCreatureReady(player1, new KitsaOtterballElite());
        Permanent rat = addCreatureReady(player1, new PestilenceRats());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentOtter = addCreatureReady(player2, new KitsaOtterballElite());

        List.of(floodcaller, bird, frog, otter, rat, bears, opponentOtter).forEach(Permanent::tap);
        int bearsPower = gqs.getEffectivePower(gd, bears);
        int bearsToughness = gqs.getEffectiveToughness(gd, bears);
        int opponentOtterPower = gqs.getEffectivePower(gd, opponentOtter);
        int opponentOtterToughness = gqs.getEffectiveToughness(gd, opponentOtter);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(List.of(floodcaller, bird, frog, otter, rat))
                .allSatisfy(permanent -> {
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(
                            permanent.getCard().getPower() + 1);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(
                            permanent.getCard().getToughness() + 1);
                });
        assertThat(bears.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(bearsPower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(bearsToughness);
        assertThat(opponentOtter.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentOtter)).isEqualTo(opponentOtterPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentOtter)).isEqualTo(opponentOtterToughness);
    }
}
