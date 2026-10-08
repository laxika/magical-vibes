package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.Excruciator;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SokratesAthenianTeacher.class, HillGiant.class, Forest.class,
        Excruciator.class, LeylineOfPunishment.class, ProdigalPyromancer.class})
class SokratesAthenianTeacherTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof while untapped")
    void hasHexproofWhileUntapped() {
        Permanent sokrates = harness.addToBattlefieldAndReturn(player1, new SokratesAthenianTeacher());

        assertThat(gqs.hasKeyword(gd, sokrates, Keyword.HEXPROOF)).isTrue();

        sokrates.tap();

        assertThat(gqs.hasKeyword(gd, sokrates, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Prevents combat damage and both players draw half, rounded down")
    void preventsCombatDamageAndDrawsHalf() {
        Permanent sokrates = addCreatureReady(player1, new SokratesAthenianTeacher());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());

        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, hillGiant.getId());
        harness.passBothPriorities();

        hillGiant.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(player1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(player2HandBefore + 1);
        assertThat(sokrates.isTapped()).isTrue();
    }

    @Test
    void drawsEvenWhenDamageCannotBePreventedGlobally() {
        addCreatureReady(player1, new SokratesAthenianTeacher());
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        harness.addToBattlefield(player2, new LeylineOfPunishment());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1 + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2 + 1);
    }

    @Test
    void sourceWithUnpreventableDamageStillDealsDamageAndBothPlayersDraw() {
        addCreatureReady(player1, new SokratesAthenianTeacher());
        Permanent attacker = addCreatureReady(player1, new Excruciator());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 13);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1 + 3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2 + 3);
    }

    @Test
    void canGrantDialogueToOpponentsCreature() {
        addCreatureReady(player1, new SokratesAthenianTeacher());
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1 + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2 + 1);
    }

    @Test
    void dialogueDoesNotPreventCombatDamageToCreatures() {
        addCreatureReady(player1, new SokratesAthenianTeacher());
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.getBlockingTargetIds().add(attacker.getId());
        harness.resolveCombatDamage();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2);
    }

    @Test
    void grantedDialogueSurvivesSokratesLeavingBattlefield() {
        Permanent sokrates = addCreatureReady(player1, new SokratesAthenianTeacher());
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(sokrates);
        gd.playerGraveyards.get(player1.getId()).add(sokrates.getCard());
        attacker.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1 + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2 + 1);
    }

    @Test
    void dialogueExpiresAtEndOfTurn() {
        addCreatureReady(player1, new SokratesAthenianTeacher());
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();

        attacker.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2);
    }

    @Test
    void tapAbilityCannotBeActivatedWhileSummoningSick() {
        harness.addToBattlefield(player1, new SokratesAthenianTeacher());
        Permanent target = addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dialogueCannotTargetLand() {
        addCreatureReady(player1, new SokratesAthenianTeacher());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void oneCombatDamageIsPreventedWithoutDrawingCards() {
        addCreatureReady(player1, new SokratesAthenianTeacher());
        Permanent attacker = addCreatureReady(player1, new ProdigalPyromancer());
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2);
    }

    @Test
    void dialogueDoesNotPreventNoncombatDamageToPlayer() {
        addCreatureReady(player1, new SokratesAthenianTeacher());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, pyromancer.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2);
    }

    @Test
    void opponentCanTargetSokratesOnlyWhileTapped() {
        Permanent sokrates = addCreatureReady(player1, new SokratesAthenianTeacher());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, sokrates.getId()))
                .isInstanceOf(IllegalStateException.class);
        sokrates.tap();
        harness.activateAbility(player2, 0, null, sokrates.getId());
        harness.passBothPriorities();

        assertThat(sokrates.getMarkedDamage()).isEqualTo(1);
        sokrates.untap();
        assertThat(gqs.hasKeyword(gd, sokrates, Keyword.HEXPROOF)).isTrue();
    }
}
