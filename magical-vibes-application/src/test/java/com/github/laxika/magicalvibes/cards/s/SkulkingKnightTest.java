package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkulkingKnight.class, Shock.class, IcyManipulator.class, GrizzlyBears.class})
class SkulkingKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new SkulkingKnight());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, knight.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skulking Knight");
        harness.assertInGraveyard(player1, "Skulking Knight");
    }

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of an activated ability")
    void sacrificesWhenTargetedByAbility() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new SkulkingKnight());
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        icyManipulator.setSummoningSick(false);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator),
                null, knight.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skulking Knight");
        harness.assertInGraveyard(player1, "Skulking Knight");
    }

    @Test
    @DisplayName("Stays on the battlefield when it is not targeted")
    void staysWhenNotTargeted() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new SkulkingKnight());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(knight.getId()));
    }

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void blockerWithoutFlankingGetsMinusOneMinusOne() {
        Permanent knight = new Permanent(new SkulkingKnight());
        knight.setSummoningSick(false);
        knight.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(knight);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }
}
