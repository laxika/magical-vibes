package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BessieTheDoctorsRoadster.class, ArvadTheCursed.class, GrizzlyBears.class})
class BessieTheDoctorsRoadsterTest extends BaseCardTest {

    @Test
    void crewAnimatesBessieAndTapsCrew() {
        Permanent bessie = addCreatureReady(player1, new BessieTheDoctorsRoadster());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bessie)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackTriggerTargetsAnotherLegendaryCreature() {
        Permanent bessie = addCreatureReady(player1, new BessieTheDoctorsRoadster());
        Permanent legendaryCreature = addCreatureReady(player1, new ArvadTheCursed());
        Permanent nonlegendaryCreature = addCreatureReady(player1, new GrizzlyBears());
        bessie.setAnimatedUntilEndOfTurn(true);
        bessie.setAnimatedPower(3);
        bessie.setAnimatedToughness(4);

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .containsExactly(legendaryCreature.getId())
                .doesNotContain(bessie.getId(), nonlegendaryCreature.getId());

        harness.handlePermanentChosen(player1, legendaryCreature.getId());
        harness.passBothPriorities();

        assertThat(legendaryCreature.isCantBeBlocked()).isTrue();
        assertThat(nonlegendaryCreature.isCantBeBlocked()).isFalse();
        assertThat(bessie.isCantBeBlocked()).isFalse();
    }

    @Test
    void newlyEnteredBessieCanAttackAfterBeingCrewedBySummoningSickCreature() {
        Permanent bessie = harness.addToBattlefieldAndReturn(player1, new BessieTheDoctorsRoadster());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, bessie)).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, bessie)).isTrue();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(bessie.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void crewCannotBePaidWithTappedCreatureOrOpponentsCreature() {
        Permanent bessie = addCreatureReady(player1, new BessieTheDoctorsRoadster());
        Permanent tappedCrew = addCreatureReady(player1, new GrizzlyBears());
        tappedCrew.tap();
        Permanent opposingCrew = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, bessie)).isFalse();
        assertThat(opposingCrew.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void crewAnimationExpiresAtEndOfTurn() {
        Permanent bessie = addCreatureReady(player1, new BessieTheDoctorsRoadster());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, bessie)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, bessie)).isFalse();
    }

    @Test
    void attackCanMakeOpposingLegendaryCreatureUnblockableOnlyForThisTurn() {
        Permanent bessie = addCreatureReady(player1, new BessieTheDoctorsRoadster());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new ArvadTheCursed());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
        assertThat(bessie.isCantBeBlocked()).isFalse();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void attackTriggerResolvesAfterBessieLeavesBattlefield() {
        Permanent bessie = addCreatureReady(player1, new BessieTheDoctorsRoadster());
        Permanent target = addCreatureReady(player1, new ArvadTheCursed());
        bessie.setAnimatedUntilEndOfTurn(true);
        bessie.setAnimatedPower(3);
        bessie.setAnimatedToughness(4);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bessie);
        gd.playerGraveyards.get(player1.getId()).add(bessie.getCard());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }
}
