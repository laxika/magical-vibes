package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlewandOak.class, Forest.class, Island.class, LeafGilder.class})
class BattlewandOakTest extends BaseCardTest {

    private Permanent addOak() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new BattlewandOak());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return oak;
    }

    @Test
    @DisplayName("Gets +2/+2 when a Forest you control enters")
    void pumpsWhenForestEnters() {
        Permanent oak = addOak();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not pump when a non-Forest land enters")
    void noPumpForNonForestLand() {
        Permanent oak = addOak();

        harness.setHand(player1, List.of(new Island()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +2/+2 when you cast a Treefolk spell")
    void pumpsWhenTreefolkCast() {
        Permanent oak = addOak();

        // A second Battlewand Oak is a Treefolk spell.
        harness.setHand(player1, List.of(new BattlewandOak()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        // Cast trigger sits on the stack above the creature spell.
        harness.passBothPriorities(); // resolve the cast trigger (pump)

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not pump when you cast a non-Treefolk spell")
    void noPumpForNonTreefolkSpell() {
        Permanent oak = addOak();

        harness.setHand(player1, List.of(new LeafGilder()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // No cast trigger — only the creature spell is on the stack.
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(3);
    }

    @Test
    void opponentForestDoesNotTrigger() {
        Permanent oak = addOak();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(3);
    }

    @Test
    void opponentTreefolkSpellDoesNotTrigger() {
        Permanent oak = addOak();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BattlewandOak()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(3);
    }

    @Test
    void treefolkEnteringWithoutBeingCastDoesNotTrigger() {
        Permanent oak = addOak();

        Permanent enteringOak = harness.enterBattlefieldAndReturn(player1, new BattlewandOak());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enteringOak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enteringOak)).isEqualTo(3);
    }

    @Test
    void bothAbilitiesAccumulateAndExpireAtEndOfTurn() {
        Permanent oak = addOak();
        harness.setHand(player1, List.of(new Forest(), new BattlewandOak()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(7);
        harness.passBothPriorities();
        Permanent secondOak = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.getEffectivePower(gd, secondOak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, secondOak)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(3);
    }
}
