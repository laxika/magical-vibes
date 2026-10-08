package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.n.NyxbornBrute;
import com.github.laxika.magicalvibes.cards.u.UnderworldRageHound;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderingChariot.class, NyxbornCourser.class, NyxbornBrute.class, UnderworldRageHound.class})
class ThunderingChariotTest extends BaseCardTest {

    @Test
    void crewAnimatesChariotAndTapsCrew() {
        Permanent chariot = addReadyChariot(player1);
        Permanent crew = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, chariot)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent chariot = addReadyChariot(player1);
        addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, chariot)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, chariot)).isFalse();
    }

    private Permanent addReadyChariot(Player player) {
        return addCreatureReady(player, new ThunderingChariot());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new NyxbornCourser());
    }

    @Test
    void crewPaysImmediatelyButAnimationWaitsForResolution() {
        Permanent chariot = addReadyChariot(player1);
        Permanent crew = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(chariot.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, chariot)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, chariot)).isTrue();
    }

    @Test
    void summoningSickCreatureCanCrewAndHasteAllowsImmediateAttack() {
        Permanent chariot = harness.addToBattlefieldAndReturn(player1, new ThunderingChariot());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chariot);
        harness.assertLife(player2, 17);
    }

    @Test
    void firstStrikeKillsLethalBlockerBeforeItDealsDamage() {
        Permanent chariot = addReadyChariot(player1);
        addReadyCreature(player1);
        Permanent blocker = addCreatureReady(player2, new NyxbornBrute());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chariot);
        assertThat(chariot.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Nyxborn Brute");
        harness.assertLife(player2, 20);
    }

    @Test
    void trampleDealsExcessFirstStrikeDamageWithoutDealingDamageAgain() {
        Permanent chariot = addReadyChariot(player1);
        addReadyCreature(player1);
        Permanent blocker = addCreatureReady(player2, new UnderworldRageHound());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 1, player2.getId(), 2));
        resolveCombat();

        harness.assertInGraveyard(player2, "Underworld Rage-Hound");
        assertThat(chariot.getMarkedDamage()).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotCrewWithTappedCreatureOrOpponentsCreature() {
        Permanent chariot = addReadyChariot(player1);
        Permanent tappedCrew = addReadyCreature(player1);
        tappedCrew.tap();
        Permanent opposingCrew = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, chariot)).isFalse();
        assertThat(opposingCrew.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTapAdditionalCrewAfterMeetingPowerRequirement() {
        Permanent chariot = addReadyChariot(player1);
        Permanent firstCrew = addReadyCreature(player1);
        Permanent secondCrew = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstCrew.getId());
        harness.handlePermanentChosen(player1, secondCrew.getId());
        harness.passBothPriorities();

        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(chariot.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, chariot)).isTrue();
    }
}
