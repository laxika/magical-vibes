package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AcademyManufactor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HardEvidence;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.s.SparkDouble;
import com.github.laxika.magicalvibes.cards.o.OjerTaqDeepestFoundation;
import com.github.laxika.magicalvibes.cards.s.SaheeliRai;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChatterfangSquirrelGeneral.class, RaiseTheAlarm.class, GrizzlyBears.class, Forest.class,
        HardEvidence.class, AcademyManufactor.class, CacklingCounterpart.class, SparkDouble.class})
class ChatterfangSquirrelGeneralTest extends BaseCardTest {

    @Test
    @CardUsed({OjerTaqDeepestFoundation.class, ThrabenInspector.class})
    void creatureOnlyMultiplierAppliesToSquirrelsAddedToClueCreation() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());
        harness.setHand(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Squirrel")).hasSize(3);
    }

    @Test
    @CardUsed(OjerTaqDeepestFoundation.class)
    void multiplierAlreadyAppliedToCreatureTokensDoesNotApplyAgainToAddedSquirrels() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Soldier")).hasSize(6);
        assertThat(findPermanents(player1, "Squirrel")).hasSize(6);
    }

    @Test
    @CardUsed({OjerTaqDeepestFoundation.class, SaheeliRai.class, SolRing.class})
    void copiedArtifactAddsMultipliedSquirrelsWithHasteAndDelayedExile() {
        Permanent saheeli = harness.addToBattlefieldAndReturn(player1, new SaheeliRai());
        saheeli.setCounterCount(CounterType.LOYALTY, 5);
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, 1, null, ring.getId());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(2);
        assertThat(findPermanents(player1, "Squirrel")).hasSize(3)
                .allSatisfy(squirrel -> assertThat(gqs.hasKeyword(gd, squirrel, Keyword.HASTE)).isTrue());
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            harness.passUntil(TurnStep.END_STEP);
            resolveAllTriggers();
        });
        assertThat(findPermanents(player1, "Squirrel")).isEmpty();
        assertThat(findPermanents(player1, "Sol Ring")).containsExactly(ring);
    }

    @Test
    @DisplayName("Creates one Squirrel for each token created under its controller's control")
    void addsSquirrelsForEachCreatedToken() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        assertThat(findPermanents(player1, "Squirrel")).hasSize(2);
    }

    @Test
    @DisplayName("Sacrifices X Squirrels to give a creature +X/-X")
    void sacrificesSquirrelsForPowerAndToughnessChange() {
        Permanent chatterfang = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);

        List<Permanent> squirrels = findPermanents(player1, "Squirrel");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, indexOf(chatterfang), 0, 2, target.getId());
        harness.handlePermanentChosen(player1, squirrels.get(0).getId());
        harness.handlePermanentChosen(player1, squirrels.get(1).getId());

        assertThat(findPermanents(player1, "Squirrel")).isEmpty();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Requires a creature target")
    void rejectsNonCreatureTarget() {
        Permanent chatterfang = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(chatterfang), 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void addsSquirrelsForCreatureAndArtifactTokens() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        harness.setHand(player1, List.of(new HardEvidence()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Crab")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Squirrel")).hasSize(2);
    }

    @Test
    void doesNotAddSquirrelsForOpponentTokens() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        harness.setHand(player2, List.of(new HardEvidence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Crab")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Squirrel")).isEmpty();
        assertThat(findPermanents(player2, "Squirrel")).isEmpty();
    }

    @Test
    void addsSquirrelWhenCreatingTokenCopy() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(findPermanents(player1, "Academy Manufactor")).hasSize(2);
        assertThat(findPermanents(player1, "Squirrel")).hasSize(1);
    }

    @Test
    void multipleChatterfangsEachReplaceTokenCreation() {
        Permanent chatterfang = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());
        harness.setHand(player1, List.of(new SparkDouble()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, chatterfang.getId());
        assertThat(findPermanents(player1, "Chatterfang, Squirrel General")).hasSize(2);

        harness.setHand(player1, List.of(new HardEvidence()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Crab")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Squirrel")).hasSize(6);
    }

    @Test
    void canSacrificeChatterfangItselfAndAbilityStillResolves() {
        Permanent chatterfang = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, indexOf(chatterfang), 0, 1, target.getId());
        harness.assertInGraveyard(player1, "Chatterfang, Squirrel General");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void cannotPaySacrificeCostWithOpponentSquirrel() {
        Permanent chatterfang = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());
        harness.addToBattlefield(player2, new ChatterfangSquirrelGeneral());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(chatterfang),
                0, 2, chatterfang.getId())).isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Chatterfang, Squirrel General");
        harness.assertOnBattlefield(player2, "Chatterfang, Squirrel General");
    }

    @Test
    void canChooseZeroWithoutSacrificingAnything() {
        Permanent chatterfang = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, indexOf(chatterfang), 0, 0, chatterfang.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chatterfang, Squirrel General");
        assertThat(gqs.getEffectivePower(gd, chatterfang)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chatterfang)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void forestwalkPreventsBlockingOnlyWhenDefenderControlsForest() {
        Permanent chatterfang = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AcademyManufactor());

        assertThat(bls.canBlockAttacker(gd, blocker, chatterfang,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        harness.addToBattlefield(player1, new Forest());
        assertThat(bls.canBlockAttacker(gd, blocker, chatterfang,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        harness.addToBattlefield(player2, new Forest());
        assertThat(bls.canBlockAttacker(gd, blocker, chatterfang,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
