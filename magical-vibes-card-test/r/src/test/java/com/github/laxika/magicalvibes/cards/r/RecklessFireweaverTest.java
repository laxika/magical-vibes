package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.ServoExhibition;
import com.github.laxika.magicalvibes.cards.w.WeldingSparks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessFireweaver.class, Ornithopter.class, ServoExhibition.class, WeldingSparks.class, PropheticPrism.class})
class RecklessFireweaverTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact entering under your control deals 1 damage to each opponent")
    void allyArtifactEntryDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new RecklessFireweaver());
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not trigger")
    void opponentArtifactEntryDoesNotDealDamage() {
        harness.addToBattlefield(player1, new RecklessFireweaver());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each artifact token entering simultaneously triggers separately")
    void artifactTokensEachTrigger() {
        harness.addToBattlefield(player1, new RecklessFireweaver());

        harness.castFromHand(player1, new ServoExhibition(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A nonartifact creature entering does not trigger either Fireweaver")
    void nonartifactEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new RecklessFireweaver());

        harness.castFromHand(player1, new RecklessFireweaver(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The triggered damage resolves after Fireweaver is destroyed")
    void damageResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new RecklessFireweaver());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new WeldingSparks()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Reckless Fireweaver"));
        harness.assertInGraveyard(player1, "Reckless Fireweaver");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A noncreature artifact entering also triggers Fireweaver")
    void noncreatureArtifactEntryTriggers() {
        harness.addToBattlefield(player1, new RecklessFireweaver());
        harness.setLibrary(player1, List.of(new RecklessFireweaver()));

        harness.castFromHand(player1, new PropheticPrism(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prophetic Prism");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }
}
