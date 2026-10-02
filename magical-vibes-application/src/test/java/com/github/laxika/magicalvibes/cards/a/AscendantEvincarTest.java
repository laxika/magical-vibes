package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.ShiftingSky;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AscendantEvincar.class, DrudgeSkeletons.class, GrizzlyBears.class, Ornithopter.class, ShiftingSky.class})
class AscendantEvincarTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new AscendantEvincar(), "{4}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Ascendant Evincar onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new AscendantEvincar(), "{4}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ascendant Evincar");
    }

    @Test
    @DisplayName("Enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.castFromHand(player1, new AscendantEvincar(), "{4}{B}{B}");
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Ascendant Evincar");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Static effect: does not buff itself =====

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent evincar = addCreatureReady(player1, new AscendantEvincar());

        assertThat(gqs.getEffectivePower(gd, evincar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, evincar)).isEqualTo(3);
    }

    // ===== Static effect: buffs other black creatures =====

    @Test
    @DisplayName("Own black creatures get +1/+1")
    void buffsOwnBlackCreatures() {
        addCreatureReady(player1, new AscendantEvincar());
        Permanent skeletons = addCreatureReady(player1, new DrudgeSkeletons());

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's black creatures also get +1/+1")
    void buffsOpponentBlackCreatures() {
        addCreatureReady(player1, new AscendantEvincar());
        Permanent opponentSkeletons = addCreatureReady(player2, new DrudgeSkeletons());

        assertThat(gqs.getEffectivePower(gd, opponentSkeletons)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSkeletons)).isEqualTo(2);
    }

    // ===== Static effect: debuffs nonblack creatures =====

    @Test
    @DisplayName("Own nonblack creatures get -1/-1")
    void debuffsOwnNonblackCreatures() {
        addCreatureReady(player1, new AscendantEvincar());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's nonblack creatures get -1/-1")
    void debuffsOpponentNonblackCreatures() {
        addCreatureReady(player1, new AscendantEvincar());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Colorless creatures get -1/-1")
    void debuffsColorlessCreatures() {
        addCreatureReady(player1, new AscendantEvincar());
        Permanent ornithopter = addCreatureReady(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(1);
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Ascendant Evincars buff each other")
    void twoEvincarsBuffEachOther() {
        Permanent firstEvincar = addCreatureReady(player1, new AscendantEvincar());
        Permanent secondEvincar = addCreatureReady(player2, new AscendantEvincar());

        assertThat(gqs.getEffectivePower(gd, firstEvincar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstEvincar)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondEvincar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondEvincar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Ascendant Evincars give +2/+2 to other black creatures")
    void twoEvincarsStackBlackBonus() {
        addCreatureReady(player1, new AscendantEvincar());
        addCreatureReady(player2, new AscendantEvincar());
        Permanent skeletons = addCreatureReady(player1, new DrudgeSkeletons());

        // 1/1 base + 2/2 from two Evincars = 3/3
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two Ascendant Evincars give -2/-2 to nonblack creatures")
    void twoEvincarsStackNonblackPenalty() {
        addCreatureReady(player1, new AscendantEvincar());
        addCreatureReady(player2, new AscendantEvincar());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        // 2/2 base - 2/2 from two Evincars = 0/0
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(0);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Black creature bonus is removed when Ascendant Evincar leaves")
    void blackBonusRemovedWhenSourceLeaves() {
        Permanent evincar = addCreatureReady(player1, new AscendantEvincar());
        Permanent skeletons = addCreatureReady(player1, new DrudgeSkeletons());

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(evincar);

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonblack creature penalty is removed when Ascendant Evincar leaves")
    void nonblackPenaltyRemovedWhenSourceLeaves() {
        Permanent evincar = addCreatureReady(player1, new AscendantEvincar());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(evincar);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Bonus applies on resolve =====

    @Test
    @DisplayName("Bonus applies when Ascendant Evincar resolves onto battlefield")
    void bonusAppliesOnResolve() {
        Permanent skeletons = addCreatureReady(player1, new DrudgeSkeletons());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        // Before casting, no bonus
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.castFromHand(player1, new AscendantEvincar(), "{4}{B}{B}");
        harness.passBothPriorities();

        // After resolving, black creature buffed, nonblack debuffed
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        addCreatureReady(player1, new AscendantEvincar());
        Permanent skeletons = addCreatureReady(player1, new DrudgeSkeletons());

        // Simulate a temporary spell boost
        skeletons.setPowerModifier(skeletons.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(5); // 1 base + 3 spell + 1 static

        // Reset end-of-turn modifiers
        skeletons.resetModifiers();

        // Spell bonus gone, static bonus still computed
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2); // 1 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ascendant Evincar gives itself -1/-1 when it becomes nonblack")
    void nonblackEvincarDebuffsItself() {
        Permanent evincar = addCreatureReady(player1, new AscendantEvincar());
        addCreatureReady(player2, new DrudgeSkeletons());

        harness.castFromHand(player1, new ShiftingSky(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.getEffectivePower(gd, evincar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, evincar)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Creatures changing to black switch from the penalty to the bonus")
    void creaturesBecomingBlackReceiveBonus() {
        Permanent evincar = addCreatureReady(player1, new AscendantEvincar());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        harness.castFromHand(player1, new ShiftingSky(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gqs.getEffectivePower(gd, evincar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, evincar)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving a second Evincar kills creatures reduced to zero toughness")
    void resolvingSecondEvincarKillsNonblackCreatures() {
        addCreatureReady(player1, new AscendantEvincar());
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AscendantEvincar(), "{4}{B}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ascendant Evincar");
        harness.assertOnBattlefield(player2, "Ascendant Evincar");
    }
}
