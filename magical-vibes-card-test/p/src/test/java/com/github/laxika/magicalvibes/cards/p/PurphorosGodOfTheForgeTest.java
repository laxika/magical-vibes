package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HammerOfPurphoros;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PurphorosGodOfTheForge.class, GrizzlyBears.class, RagingGoblin.class,
        WrathOfGod.class, HammerOfPurphoros.class, Mountain.class, MycosynthLattice.class})
class PurphorosGodOfTheForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Purphoros is not a creature below five devotion to red")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent purphoros = addPurphoros();
        addRedPermanents(3);

        assertThat(gqs.isCreature(gd, purphoros)).isFalse();
        assertThat(gqs.isEnchantment(gd, purphoros)).isTrue();
    }

    @Test
    @DisplayName("Purphoros becomes a creature at five devotion to red")
    void becomesCreatureAtDevotionThreshold() {
        Permanent purphoros = addPurphoros();
        addRedPermanents(4);

        assertThat(gqs.isCreature(gd, purphoros)).isTrue();
    }

    @Test
    @DisplayName("Another creature entering under your control deals 2 damage to each opponent")
    void damagesEachOpponentWhenAllyCreatureEnters() {
        harness.setLife(player2, 20);
        addPurphoros();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A creature entering under an opponent's control does not trigger Purphoros")
    void doesNotTriggerForOpponentCreature() {
        harness.setLife(player2, 20);
        addPurphoros();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Paying {2}{R} gives creatures you control +1/+0 until end of turn")
    void boostsOwnCreaturesUntilEndOfTurn() {
        Permanent purphoros = addPurphoros();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, purphoros)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Purphoros does not trigger for its own entry even with sufficient devotion")
    void doesNotTriggerForItsOwnEntry() {
        addRedPermanents(4);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new PurphorosGodOfTheForge(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        Permanent purphoros = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.isCreature(gd, purphoros)).isTrue();
    }

    @Test
    @DisplayName("Indestructible Purphoros survives a creature sweeper and immediately loses creature status")
    void survivesDestructionAndLosesCreatureStatusWhenDevotionFalls() {
        Permanent purphoros = addPurphoros();
        addRedPermanents(4);
        assertThat(gqs.isCreature(gd, purphoros)).isTrue();

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(purphoros);
        assertThat(gqs.isCreature(gd, purphoros)).isFalse();
        assertThat(gqs.isEnchantment(gd, purphoros)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, purphoros, CardSubtype.GOD)).isFalse();
    }

    @Test
    @DisplayName("Opponents' red permanents do not contribute to Purphoros's devotion")
    void ignoresOpponentsDevotion() {
        Permanent purphoros = addPurphoros();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new RagingGoblin());
        }

        assertThat(gqs.isCreature(gd, purphoros)).isFalse();
    }

    @Test
    @DisplayName("The creature reaching five devotion triggers Purphoros without damaging its controller")
    void triggersWhenEnteringCreatureReachesDevotionThreshold() {
        Permanent purphoros = addPurphoros();
        addRedPermanents(3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, purphoros)).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The boost includes creature Purphoros, excludes opponents, and multiple activations stack")
    void repeatedActivationsBoostOnlyOwnCreaturesIncludingPurphoros() {
        Permanent purphoros = addPurphoros();
        addRedPermanents(4);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, purphoros)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, purphoros)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after the boost resolves are not boosted")
    void doesNotBoostLaterEntrants() {
        addPurphoros();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent laterBears = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, laterBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncreature Purphoros is not retroactively boosted when devotion increases")
    void doesNotRetroactivelyBoostPurphoros() {
        Permanent purphoros = addPurphoros();
        addRedPermanents(3);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, purphoros)).isTrue();
        assertThat(gqs.getEffectivePower(gd, purphoros)).isEqualTo(6);
    }

    @Test
    @DisplayName("Noncreature entry does not trigger Purphoros, but a creature token does")
    void triggersForCreatureTokensButNotNoncreaturePermanents() {
        Permanent purphoros = addPurphoros();
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new HammerOfPurphoros(), "{1}{R}{R}");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gqs.isCreature(gd, purphoros)).isFalse();

        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Golem");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Losing creature status preserves artifact status granted by an earlier Mycosynth Lattice")
    void preservesOtherCardTypesBelowDevotionThreshold() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        Permanent purphoros = addPurphoros();

        assertThat(gqs.isCreature(gd, purphoros)).isFalse();
        assertThat(gqs.isEnchantment(gd, purphoros)).isTrue();
        assertThat(gqs.isArtifact(gd, purphoros)).isTrue();
    }

    private Permanent addPurphoros() {
        return harness.addToBattlefieldAndReturn(player1, new PurphorosGodOfTheForge());
    }

    private void addRedPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new RagingGoblin());
        }
    }
}
