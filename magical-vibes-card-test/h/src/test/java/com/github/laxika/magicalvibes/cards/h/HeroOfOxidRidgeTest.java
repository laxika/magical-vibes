package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GlissasCourier;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.m.MyrSire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeroOfOxidRidge.class, MyrSire.class, GlissasCourier.class})
class HeroOfOxidRidgeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new HeroOfOxidRidge());

        declareAttackers(player1, List.of(0));

        // At minimum, the ON_ATTACK trigger should be on the stack (battle cry may also add one)
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Hero of Oxid Ridge"));
    }

    @Test
    @DisplayName("Creatures with power 1 or less are marked can't block this turn")
    void creaturesWithPower1OrLessCantBlock() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent elves = addCreatureReady(player2, new MyrSire()); // 1/1

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bls.canBlockAttacker(gd, elves, hero,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Creatures with power 2 or more are NOT affected")
    void creaturesWithPower2OrMoreNotAffected() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent bears = addCreatureReady(player2, new GlissasCourier()); // 2/3

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bls.canBlockAttacker(gd, bears, hero,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Effect applies to all players' creatures with power 1 or less")
    void affectsAllPlayersCreatures() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent ownElves = addCreatureReady(player1, new MyrSire());   // own 1/1
        Permanent oppElves = addCreatureReady(player2, new MyrSire());   // opponent 1/1
        Permanent oppBears = addCreatureReady(player2, new GlissasCourier());    // opponent 2/3

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bls.canBlockAttacker(gd, ownElves, oppBears,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oppElves, hero,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oppBears, hero,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Power-1-or-less creature cannot declare as blocker after trigger resolves")
    void cantBlockPreventsDeclaringBlockers() {
        addCreatureReady(player1, new HeroOfOxidRidge());
        addCreatureReady(player2, new MyrSire()); // 1/1

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        prepareDeclareBlockers();

        // Attempting to block with the 1/1 should be rejected
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Power-2-or-more creature CAN still block after trigger resolves")
    void power2OrMoreCanStillBlock() {
        addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent oppBears = addCreatureReady(player2, new GlissasCourier()); // 2/3

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        prepareDeclareBlockers();

        // Glissa's Courier (2/3) should be able to block
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(oppBears.isBlocking()).isTrue();
    }

    @Test
    void hasteAllowsAttackingWhileSummoningSick() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfOxidRidge());
        hero.setSummoningSick(true);

        declareAttackers(List.of(0));

        assertThat(hero.isAttacking()).isTrue();
        resolveAllTriggers();
    }

    @Test
    void battleCryBoostsOnlyOtherAttackersAndExpiresAtCleanup() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent attacker = addCreatureReady(player1, new GlissasCourier());
        Permanent nonattacker = addCreatureReady(player1, new MyrSire());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);
        assertThat(hero.getPowerModifier()).isZero();
        assertThat(nonattacker.getPowerModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void restrictionUsesCurrentPowerWhenDeclaringBlockers() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent small = addCreatureReady(player2, new MyrSire());
        Permanent large = addCreatureReady(player2, new GlissasCourier());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        small.setPowerModifier(1);
        large.setPowerModifier(-1);
        prepareDeclareBlockers();

        assertThat(bls.canBlockAttacker(gd, small, hero, gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, large, hero, gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void restrictionAppliesToCreaturesEnteringAfterResolution() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        Permanent lateCreature = addCreatureReady(player2, new MyrSire());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bls.canBlockAttacker(gd, lateCreature, hero, gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void blockingRestrictionExpiresAtCleanup() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent small = addCreatureReady(player2, new MyrSire());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(bls.canBlockAttacker(gd, small, hero, gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, small, hero, gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
    @Test
    @CardUsed({GoForTheThroat.class})
    void attackTriggersResolveAfterHeroIsDestroyed() {
        Permanent hero = addCreatureReady(player1, new HeroOfOxidRidge());
        Permanent attacker = addCreatureReady(player1, new GlissasCourier());
        Permanent small = addCreatureReady(player2, new MyrSire());
        declareAttackers(List.of(0, 1));

        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, hero.getId());
        harness.assertInGraveyard(player1, "Hero of Oxid Ridge");
        resolveAllTriggers();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(bls.canBlockAttacker(gd, small, attacker, gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

}
