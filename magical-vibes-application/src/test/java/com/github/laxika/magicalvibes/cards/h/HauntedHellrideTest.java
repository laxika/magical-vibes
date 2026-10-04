package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntedHellride.class, GrizzlyBears.class})
class HauntedHellrideTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you attack, a creature you control gets boosted, gains deathtouch, and untaps")
    void boostsGivesDeathtouchAndUntapsTargetYouControl() {
        addCreatureReady(player1, new HauntedHellride());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, attacker);

        declareAttackers(player1, List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attacker.getId()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(originalPower + 1);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(attacker.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(originalPower);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent vehicle = addCreatureReady(player1, new HauntedHellride());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    void multipleAttackersTriggerOnceAndCanUntapANonattackingCreature() {
        Permanent vehicle = addCreatureReady(player1, new HauntedHellride());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        addCreatureReady(player2, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        declareAttackers(List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(vehicle.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(firstAttacker.isTapped()).isTrue();
        assertThat(secondAttacker.isTapped()).isTrue();
    }

    @Test
    void attackTriggerStillResolvesAfterVehicleLeavesBattlefield() {
        Permanent vehicle = addCreatureReady(player1, new HauntedHellride());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, target);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(vehicle);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void targetThatChangesControllerIsNotBoostedOrUntapped() {
        addCreatureReady(player1, new HauntedHellride());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        addCreatureReady(player2, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, target);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void opponentsAttackDoesNotTriggerVehicle() {
        addCreatureReady(player1, new HauntedHellride());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.tap();
        addCreatureReady(player2, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, ownCreature);

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(originalPower);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(ownCreature.isTapped()).isTrue();
    }
}
