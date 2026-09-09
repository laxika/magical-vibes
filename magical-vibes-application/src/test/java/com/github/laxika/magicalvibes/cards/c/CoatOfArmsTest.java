package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Anarchist;
import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.StandingTroops;
import com.github.laxika.magicalvibes.cards.w.WelkinHawk;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoatOfArms.class, Anarchist.class, ChangelingWayfinder.class, RagingGoblin.class,
        StandingTroops.class, WelkinHawk.class})
class CoatOfArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CoatOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Coat of Arms onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new CoatOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No bonus for creatures that do not share a creature type")
    void noBonusForUnrelatedCreatures() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new WelkinHawk());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two creatures sharing a type each get +1/+1")
    void twoCreaturesSharingType() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent firstGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent secondGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, firstGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondGoblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Three creatures sharing a type each get +2/+2")
    void threeCreaturesSharingType() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent firstGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent secondGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent thirdGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, firstGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, thirdGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thirdGoblin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Coat of Arms permanents stack their bonuses")
    void multipleCoatsStackTheirBonuses() {
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player2, new CoatOfArms());
        Permanent firstGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent secondGoblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, firstGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondGoblin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures with partial type overlap get different bonuses")
    void partialTypeOverlap() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent anarchist = harness.addToBattlefieldAndReturn(player1, new Anarchist());
        Permanent standingTroops = harness.addToBattlefieldAndReturn(player1, new StandingTroops());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new WelkinHawk());

        assertThat(gqs.getEffectivePower(gd, anarchist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, anarchist)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, standingTroops)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, standingTroops)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus counts creatures on opponent's battlefield too")
    void bonusCountsOpponentCreatures() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Changeling shares a creature type with every typed creature")
    void changelingSharesTypeWithEverything() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent wayfinder = harness.addToBattlefieldAndReturn(player1, new ChangelingWayfinder());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, wayfinder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wayfinder)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two changelings share creature types with each other")
    void twoChangelingsShareTypes() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent firstWayfinder = harness.addToBattlefieldAndReturn(player1, new ChangelingWayfinder());
        Permanent secondWayfinder = harness.addToBattlefieldAndReturn(player1, new ChangelingWayfinder());

        assertThat(gqs.getEffectivePower(gd, firstWayfinder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstWayfinder)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondWayfinder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondWayfinder)).isEqualTo(3);
    }

    @Test
    @DisplayName("Changeling gets a bonus from every typed creature on the battlefield")
    void changelingBonusScalesWithAllCreatures() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent wayfinder = harness.addToBattlefieldAndReturn(player1, new ChangelingWayfinder());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new WelkinHawk());
        harness.addToBattlefield(player1, new StandingTroops());

        assertThat(gqs.getEffectivePower(gd, wayfinder)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wayfinder)).isEqualTo(5);
    }

    @Test
    @CardUsed(ArcaneAdaptation.class)
    @DisplayName("Counts a creature type granted by another continuous effect")
    void countsGrantedCreatureTypes() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent standingTroops = harness.addToBattlefieldAndReturn(player1, new StandingTroops());
        Permanent adaptation = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.HUMAN);

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, standingTroops)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, standingTroops)).isEqualTo(5);
    }

    @Test
    @DisplayName("A single creature on the battlefield gets no bonus")
    void singleCreatureNoBonus() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus is removed when Coat of Arms leaves the battlefield")
    void bonusRemovedWhenCoatLeaves() {
        Permanent coat = harness.addToBattlefieldAndReturn(player1, new CoatOfArms());
        Permanent firstGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, firstGoblin)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(coat);

        assertThat(gqs.getEffectivePower(gd, firstGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, firstGoblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus applies when Coat of Arms resolves onto the battlefield")
    void bonusAppliesOnResolve() {
        Permanent firstGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new CoatOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThat(gqs.getEffectivePower(gd, firstGoblin)).isEqualTo(1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstGoblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());

        goblin.setPowerModifier(goblin.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(5);

        goblin.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus increases when a new creature sharing a type enters")
    void bonusIncreasesWhenNewCreatureEnters() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent trackedGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, trackedGoblin)).isEqualTo(2);

        harness.addToBattlefield(player1, new WelkinHawk());
        assertThat(gqs.getEffectivePower(gd, trackedGoblin)).isEqualTo(2);

        harness.addToBattlefield(player1, new RagingGoblin());
        assertThat(gqs.getEffectivePower(gd, trackedGoblin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus decreases when a creature sharing a type leaves")
    void bonusDecreasesWhenCreatureLeaves() {
        harness.addToBattlefield(player1, new CoatOfArms());
        Permanent trackedGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent removedGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, trackedGoblin)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(removedGoblin);

        assertThat(gqs.getEffectivePower(gd, trackedGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, trackedGoblin)).isEqualTo(2);
    }
}
