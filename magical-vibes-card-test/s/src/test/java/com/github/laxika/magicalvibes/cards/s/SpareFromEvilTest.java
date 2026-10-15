package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvacynianPriest;
import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.g.Geistflame;
import com.github.laxika.magicalvibes.cards.o.OliviaVoldaren;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.r.RecklessWaif;
import com.github.laxika.magicalvibes.cards.r.RottingFensnake;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({SpareFromEvil.class, DoomedTraveler.class, WalkingCorpse.class, RottingFensnake.class,
        RecklessWaif.class, OliviaVoldaren.class, AvacynianPriest.class,
        Geistflame.class, PreyUpon.class, TurnToFrog.class})
class SpareFromEvilTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Spare from Evil puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new SpareFromEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Resolving grants protection to controlled Human and non-Human creatures")
    void resolvingGrantsProtection() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new DoomedTraveler());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        castSpareFromEvil();

        assertThat(human.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).contains(CardSubtype.HUMAN);
        assertThat(zombie.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).contains(CardSubtype.HUMAN);
        harness.assertInGraveyard(player1, "Spare from Evil");
    }

    @Test
    @DisplayName("Non-Human creature cannot block a protected attacker")
    void nonHumanCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new DoomedTraveler());
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        castSpareFromEvil();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bls.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player2.getId())))
                .isFalse();
    }

    @Test
    @DisplayName("Human creature can still block a protected attacker")
    void humanCanStillBlock() {
        Permanent attacker = addCreatureReady(player1, new DoomedTraveler());
        Permanent blocker = addCreatureReady(player2, new DoomedTraveler());
        castSpareFromEvil();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bls.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player2.getId())))
                .isTrue();
    }

    @Test
    @DisplayName("Casting after blocks prevents combat damage from a non-Human blocker")
    void combatDamageFromNonHumanPrevented() {
        Permanent attacker = addCreatureReady(player1, new DoomedTraveler());
        addCreatureReady(player2, new RottingFensnake());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.setHand(player1, List.of(new SpareFromEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.castAndResolveInstant(player1, 0));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Doomed Traveler");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Rotting Fensnake");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Protection persists through the end step and expires at cleanup")
    void protectionClearedAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        castSpareFromEvil();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(creature.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).contains(CardSubtype.HUMAN);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Does not grant protection to opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DoomedTraveler());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DoomedTraveler());

        castSpareFromEvil();

        assertThat(ownCreature.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).contains(CardSubtype.HUMAN);
        assertThat(opponentCreature.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).isEmpty();
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotGainProtection() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        castSpareFromEvil();

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new DoomedTraveler());

        assertThat(original.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).contains(CardSubtype.HUMAN);
        assertThat(newcomer.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).isEmpty();
    }

    @Test
    void creaturesEnteringBeforeResolutionGainProtection() {
        harness.setHand(player1, List.of(new SpareFromEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.passBothPriorities();

        assertThat(creature.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).contains(CardSubtype.HUMAN);
    }

    @Test
    void humanWithOtherCreatureTypesCanStillBlock() {
        Permanent attacker = addCreatureReady(player1, new DoomedTraveler());
        Permanent blocker = addCreatureReady(player2, new RecklessWaif());
        castSpareFromEvil();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bls.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player2.getId())))
                .isTrue();
    }

    @Test
    void nonHumanCreatureAbilityCannotTargetProtectedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new OliviaVoldaren());
        castSpareFromEvil();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void protectionMakesPendingNonHumanCreatureAbilityTargetIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent olivia = harness.addToBattlefieldAndReturn(player2, new OliviaVoldaren());
        harness.setHand(player1, List.of(new SpareFromEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, target.getId());

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getGrantedSubtypes()).doesNotContain(CardSubtype.VAMPIRE);
        assertThat(olivia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void humanCreatureAbilityCanStillTargetProtectedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        addCreatureReady(player2, new AvacynianPriest());
        castSpareFromEvil();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void instantCanStillTargetAndDamageProtectedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        castSpareFromEvil();
        harness.setHand(player2, List.of(new Geistflame()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    void fightDamageFromNonHumanCreatureIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent snake = harness.addToBattlefieldAndReturn(player2, new RottingFensnake());
        castSpareFromEvil();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new PreyUpon()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player2, 0, List.of(snake.getId(), target.getId()));

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertInGraveyard(player2, "Rotting Fensnake");
    }

    @Test
    void resolvesWithNoCreatures() {
        castSpareFromEvil();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spare from Evil");
    }

    @Test
    void laterAbilityRemovalRemovesGrantedProtection() {
        Permanent attacker = addCreatureReady(player1, new DoomedTraveler());
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        castSpareFromEvil();
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, attacker.getId());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bls.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player2.getId())))
                .isTrue();
    }

    @Test
    void protectionGrantedAfterAbilityRemovalStillApplies() {
        Permanent attacker = addCreatureReady(player1, new DoomedTraveler());
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, attacker.getId());

        castSpareFromEvil();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bls.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player2.getId())))
                .isFalse();
    }

    @Test
    void combatDamageFromHumanIsNotPrevented() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player2, new DoomedTraveler());
        castSpareFromEvil();
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertInGraveyard(player2, "Doomed Traveler");
    }

    private void castSpareFromEvil() {
        harness.setHand(player1, List.of(new SpareFromEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
