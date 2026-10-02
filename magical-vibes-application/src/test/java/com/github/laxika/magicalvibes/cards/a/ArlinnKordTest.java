package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DuskwatchRecruiter;
import com.github.laxika.magicalvibes.cards.h.HinterlandLogger;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArlinnKord.class, DuskwatchRecruiter.class, HinterlandLogger.class})
class ArlinnKordTest extends BaseCardTest {

    @CardUsed({ArlinnKord.class, DuskwatchRecruiter.class, HinterlandLogger.class})
    @Nested
    @DisplayName("Front face +1: up to one creature +2/+2 vigilance haste")
    class FrontPlusOne {

        @Test
        @DisplayName("Pumps target creature and grants vigilance and haste")
        void pumpsAndGrantsKeywords() {
            Permanent arlinn = addFrontFace(player1, 3);
            Permanent target = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 0, target.getId(), null);
            harness.passBothPriorities();

            assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
            assertThat(target.hasKeyword(Keyword.VIGILANCE)).isTrue();
            assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        }

        @Test
        @DisplayName("Can activate with no target")
        void canActivateWithNoTarget() {
            Permanent arlinn = addFrontFace(player1, 3);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 0, null, null);
            harness.passBothPriorities();

            assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        }
    }

    @CardUsed({ArlinnKord.class, DuskwatchRecruiter.class, HinterlandLogger.class})
    @Nested
    @DisplayName("Front face 0: Wolf token and transform")
    class FrontZero {

        @Test
        @DisplayName("Creates a 2/2 green Wolf and transforms")
        void createsWolfAndTransforms() {
            Permanent arlinn = addFrontFace(player1, 3);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 1, null, null);
            harness.passBothPriorities();

            assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
            assertThat(arlinn.isTransformed()).isTrue();
            assertThat(arlinn.getCard().getName()).isEqualTo("Arlinn, Embraced by the Moon");

            Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                    .findFirst().orElseThrow();
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WOLF);
        }
    }

    @CardUsed({ArlinnKord.class, DuskwatchRecruiter.class, HinterlandLogger.class})
    @Nested
    @DisplayName("Back face +1: team pump and trample")
    class BackPlusOne {

        @Test
        @DisplayName("Gives own creatures +1/+1 and trample")
        void pumpsOwnCreatures() {
            Permanent arlinn = addTransformedBackFace(player1, 3);
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());
            Permanent opp = harness.addToBattlefieldAndReturn(player2, new HinterlandLogger());

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 0, null, null);
            harness.passBothPriorities();

            assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
            assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
            assertThat(gqs.getEffectivePower(gd, opp)).isEqualTo(2);
            assertThat(opp.hasKeyword(Keyword.TRAMPLE)).isFalse();
        }
    }

    @CardUsed({ArlinnKord.class, DuskwatchRecruiter.class, HinterlandLogger.class})
    @Nested
    @DisplayName("Back face -1: damage and transform back")
    class BackMinusOne {

        @Test
        @DisplayName("Deals 3 damage to a player and transforms to front face")
        void damagesPlayerAndTransformsBack() {
            Permanent arlinn = addTransformedBackFace(player1, 3);
            int lifeBefore = gd.playerLifeTotals.get(player2.getId());

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 1, null, player2.getId());
            harness.passBothPriorities();

            assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
            assertThat(arlinn.isTransformed()).isFalse();
            assertThat(arlinn.getCard().getName()).isEqualTo("Arlinn Kord");
        }

        @Test
        @DisplayName("Deals 3 damage to a creature")
        void damagesCreature() {
            Permanent arlinn = addTransformedBackFace(player1, 3);
            Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskwatchRecruiter());

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 1, null, target.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Duskwatch Recruiter");
            assertThat(arlinn.isTransformed()).isFalse();
        }
    }

    @CardUsed({ArlinnKord.class, DuskwatchRecruiter.class, HinterlandLogger.class})
    @Nested
    @DisplayName("Back face -6: emblem")
    class BackMinusSix {

        @Test
        @DisplayName("Creates emblem with haste and tap-for-power damage")
        void createsEmblem() {
            Permanent arlinn = addTransformedBackFace(player1, 6);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 2, null, null);
            harness.passBothPriorities();

            assertThat(gd.emblems).hasSize(1);
            Emblem emblem = gd.emblems.getFirst();
            assertThat(emblem.controllerId()).isEqualTo(player1.getId());
            harness.assertNotOnBattlefield(player1, "Arlinn, Embraced by the Moon");
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());
            assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
            int creatureIdx = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
            harness.activateAbility(player1, creatureIdx, 1, null, player2.getId());
            harness.passBothPriorities();
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("Emblem grants haste and lets a creature deal power damage")
        void emblemGrantsHasteAndTapDamage() {
            Permanent arlinn = addTransformedBackFace(player1, 6);
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            harness.activateAbility(player1, idx, 2, null, null);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
            assertThat(gs.getEffectiveActivatedAbilities(gd, creature)).hasSize(2);

            int creatureIdx = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
            int lifeBefore = gd.playerLifeTotals.get(player2.getId());
            harness.activateAbility(player1, creatureIdx, 1, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
            assertThat(creature.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Cannot activate -6 with insufficient loyalty")
        void cannotActivateWithInsufficientLoyalty() {
            Permanent arlinn = addTransformedBackFace(player1, 5);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(arlinn);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, 2, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void frontPumpCanTargetOpponentsCreatureAndExpiresAtCleanup() {
        Permanent arlinn = addFrontFace(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskwatchRecruiter());
        harness.activateAbility(player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void transformationDoesNotPermitAnotherLoyaltyActivation() {
        Permanent arlinn = addFrontFace(player1, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(arlinn.isTransformed()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void invalidDamageTargetPreventsTransformation() {
        Permanent arlinn = addTransformedBackFace(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskwatchRecruiter());
        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(arlinn.isTransformed()).isTrue();
        assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void backPumpDoesNotAffectCreaturesEnteringAfterResolution() {
        addTransformedBackFace(player1, 3);
        Permanent original = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, original, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, later, Keyword.TRAMPLE)).isFalse();
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, original, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @CardUsed({ArlinnKord.class, SarkhanTheMasterless.class})
    void backPumpGrantsTrampleToArlinnWhenSheIsACreature() {
        Permanent arlinn = addTransformedBackFace(player1, 3);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, arlinn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, arlinn)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, arlinn, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void emblemUsesPowerAtResolutionAndDoesNotGrantAbilitiesToOpponent() {
        addTransformedBackFace(player1, 6);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DuskwatchRecruiter());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HASTE)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, opponent)).hasSize(1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void emblemDealsDamageUsingLastKnownPowerAfterSourceLeaves() {
        addTransformedBackFace(player1, 6);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private Permanent addFrontFace(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ArlinnKord());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addTransformedBackFace(Player player, int loyalty) {
        Permanent perm = addFrontFace(player, loyalty);
        perm.setTransformed(true);
        perm.setCard(perm.getOriginalCard().getBackFaceCard());
        return perm;
    }
}
