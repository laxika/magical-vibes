package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaiiWakeenPerfectShot.class, GrizzlyBears.class, Shock.class, ZuranSpellcaster.class, SamiteHealer.class})
class TaiiWakeenPerfectShotTest extends BaseCardTest {

    @Test
    void drawsWhenControlledSourceDealsNoncombatDamageEqualToToughness() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));

        castShockAt(target);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    void doesNotDrawWhenNoncombatDamageIsNotEqualToToughness() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        Permanent source = addCreatureReady(player1, new ZuranSpellcaster());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        prepareMainPhase();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void addsXToControlledNoncombatDamageForTheTurn() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void doesNotDrawForDamageGreaterThanToughness() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        Permanent target = addCreatureReady(player2, new ZuranSpellcaster());

        castShockAt(target);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void doesNotDrawForAnOpponentsSource() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        prepareMainPhase();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void exactDamageStillDrawsWhenTheCreatureAlreadyHasDamageMarked() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        addCreatureReady(player1, new ZuranSpellcaster());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        prepareMainPhase();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        castShockAt(target);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    void cumulativeDamageEqualToToughnessDoesNotDraw() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        addCreatureReady(player1, new ZuranSpellcaster());
        addCreatureReady(player1, new ZuranSpellcaster());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase();

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void boostedDamageCanTriggerOnTaiiItselfAndBonusSurvivesItsDeath() {
        Permanent taii = addCreatureReady(player1, new TaiiWakeenPerfectShot());
        Card draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        castShockAt(taii);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(taii);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);

        prepareMainPhase();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void bonusDoesNotIncreaseAnOpponentsDamage() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void damageRecipientChoosesWhetherPreventionAppliesBeforeTheBonus() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        addCreatureReady(player1, new ZuranSpellcaster());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new SamiteHealer());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.passPriority(player1);
        harness.activateAbility(player2, 1, null, target.getId());
        harness.passBothPriorities();
        prepareMainPhase();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void zeroXDoesNotIncreaseDamage() {
        Permanent taii = addCreatureReady(player1, new TaiiWakeenPerfectShot());
        prepareMainPhase();
        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(taii.isTapped()).isTrue();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void bonusExpiresAtTheEndOfTheTurn() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void combatDamageIsNeitherBoostedNorEligibleForTheDrawTrigger() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.stack).isEmpty();
    }

    private void castShockAt(Permanent target) {
        prepareMainPhase();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
