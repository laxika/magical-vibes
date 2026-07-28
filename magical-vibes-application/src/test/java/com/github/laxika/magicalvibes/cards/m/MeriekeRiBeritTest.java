package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeriekeRiBeritTest extends BaseCardTest {

    @Test
    @DisplayName("{T} gains control of target creature for as long as its controller controls Merieke")
    void gainsControlOfTargetCreature() {
        Permanent merieke = addReadyMerieke(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        activateSteal(merieke, bears);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(merieke.isTapped()).isTrue();
        assertThat(gd.newestControlEffectFor(bears.getId()).sourcePermanentId()).isEqualTo(merieke.getId());
    }

    @Test
    @DisplayName("Merieke leaving the battlefield destroys the stolen creature")
    void leavingDestroysStolenCreature() {
        Permanent merieke = addReadyMerieke(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        activateSteal(merieke, bears);

        // Bolt Merieke: she dies, the leaves-the-battlefield trigger destroys the stolen creature.
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, merieke.getId());
        harness.passBothPriorities(); // bolt resolves, Merieke dies, trigger goes on the stack
        harness.passBothPriorities(); // trigger resolves

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Merieke becoming untapped destroys the stolen creature")
    void untappingDestroysStolenCreature() {
        Permanent merieke = addReadyMerieke(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        activateSteal(merieke, bears);

        // Twiddle untaps Merieke — the becomes-untapped trigger destroys the stolen creature.
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, merieke.getId());
        harness.passBothPriorities(); // Twiddle resolves, Merieke untaps, trigger goes on the stack
        harness.passBothPriorities(); // trigger resolves

        assertThat(merieke.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A second untap after the stolen creature is gone destroys nothing")
    void secondUntapDestroysNothing() {
        Permanent merieke = addReadyMerieke(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());

        activateSteal(merieke, bears);

        harness.setHand(player1, List.of(new Twiddle(), new Twiddle(), new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, merieke.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Tap then untap Merieke again: the link is spent, so player2's other creature survives.
        harness.castInstant(player1, 0, merieke.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, merieke.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(other.getId()));
    }

    private Permanent addReadyMerieke(Player player) {
        Permanent perm = new Permanent(new MeriekeRiBerit());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void activateSteal(Permanent merieke, Permanent target) {
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(merieke);
        harness.activateAbility(player1, idx, null, target.getId());
        harness.passBothPriorities();
    }
}
