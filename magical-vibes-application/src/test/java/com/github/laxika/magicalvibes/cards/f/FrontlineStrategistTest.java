package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.d.DaruSpiritualist;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrontlineStrategist.class, AvenFarseer.class, DaruSpiritualist.class, ProdigalSorcerer.class})
class FrontlineStrategistTest extends BaseCardTest {

    @Test
    @DisplayName("Turning face up prevents combat damage from non-Soldier creatures")
    void turningFaceUpPreventsCombatDamageFromNonSoldiers() {
        turnFrontlineStrategistFaceUp();
        harness.setLife(player2, 20);

        Permanent nonSoldier = addCreatureReady(player1, new DaruSpiritualist());
        Permanent soldier = addCreatureReady(player1, new AvenFarseer());

        assertThat(gqs.isPreventedFromDealingDamage(gd, nonSoldier, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, soldier, true)).isFalse();

        declareAttackers(player1, List.of(1, 2));
        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Turning face up prevents combat damage from creatures controlled by either player")
    void turningFaceUpPreventsCombatDamageFromOpposingNonSoldiers() {
        turnFrontlineStrategistFaceUp();
        harness.setLife(player1, 20);

        Permanent nonSoldier = addCreatureReady(player2, new DaruSpiritualist());
        Permanent soldier = addCreatureReady(player2, new AvenFarseer());

        assertThat(gqs.isPreventedFromDealingDamage(gd, nonSoldier, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, soldier, true)).isFalse();

        declareAttackersAndPrepareBlockers(player2, List.of(0, 1));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Turning face up does not prevent noncombat damage")
    void turningFaceUpDoesNotPreventNoncombatDamage() {
        turnFrontlineStrategistFaceUp();
        harness.setLife(player1, 20);

        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Non-Soldier attackers and blockers deal no combat damage to each other")
    void preventsDamageToCreaturesFromAttackersAndBlockers() {
        turnFrontlineStrategistFaceUp();
        Permanent attacker = addCreatureReady(player1, new DaruSpiritualist());
        Permanent blocker = addCreatureReady(player2, new DaruSpiritualist());

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The face-up Strategist still deals combat damage as a Soldier")
    void faceUpStrategistDealsDamageToNonSoldierBlocker() {
        turnFrontlineStrategistFaceUp();
        Permanent strategist = findPermanent(player1, "Frontline Strategist");
        strategist.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new DaruSpiritualist());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(strategist);
        assertThat(strategist.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Daru Spiritualist");
    }

    @Test
    @DisplayName("A face-down Strategist has no Soldier subtype and its combat damage is prevented")
    void preventsDamageFromAnotherFaceDownStrategist() {
        turnFrontlineStrategistFaceUp();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FrontlineStrategist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent faceDownStrategist = gd.playerBattlefields.get(player1.getId()).get(1);
        faceDownStrategist.setSummoningSick(false);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(1));
        resolveCombat(player1);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Non-Soldier combat damage resumes on the next turn")
    void preventionExpiresAtEndOfTurn() {
        turnFrontlineStrategistFaceUp();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        addCreatureReady(player2, new DaruSpiritualist());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }

    private void turnFrontlineStrategistFaceUp() {
        harness.setHand(player1, List.of(new FrontlineStrategist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent strategist = findPermanent(player1, "Frontline Strategist");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(strategist));
        harness.passBothPriorities();
    }
}
