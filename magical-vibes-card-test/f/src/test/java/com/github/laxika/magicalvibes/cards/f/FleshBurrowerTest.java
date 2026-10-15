package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalemurkLeech;
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

@CardUsed({FleshBurrower.class, BalemurkLeech.class})
class FleshBurrowerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone needs no target and does not prevent combat")
    void attackingWithoutLegalTarget() {
        Permanent burrower = addCreatureReady(player1, new FleshBurrower());
        addCreatureReady(player2, new BalemurkLeech());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(burrower.isAttacking()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger resolves after Flesh Burrower leaves the battlefield")
    void attackTriggerSurvivesSourceRemoval() {
        Permanent burrower = addCreatureReady(player1, new FleshBurrower());
        Permanent target = addCreatureReady(player1, new BalemurkLeech());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(burrower);
        gd.playerGraveyards.get(player1.getId()).add(burrower.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("A target controlled by the opponent at resolution does not gain deathtouch")
    void targetChangingControllerBeforeResolutionBecomesIllegal() {
        addCreatureReady(player1, new FleshBurrower());
        Permanent target = addCreatureReady(player1, new BalemurkLeech());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A resolved deathtouch grant survives a subsequent control change")
    void resolvedGrantSurvivesControlChange() {
        addCreatureReady(player1, new FleshBurrower());
        Permanent target = addCreatureReady(player1, new BalemurkLeech());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Attack trigger targets another creature I control")
    void attackTriggerRestrictsTargets() {
        Permanent burrower = addCreatureReady(player1, new FleshBurrower());
        Permanent ownCreature = addCreatureReady(player1, new BalemurkLeech());
        Permanent opponentCreature = addCreatureReady(player2, new BalemurkLeech());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(burrower.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("Attack trigger gives the target deathtouch until end of turn")
    void attackTriggerGrantsDeathtouch() {
        addCreatureReady(player1, new FleshBurrower());
        Permanent target = addCreatureReady(player1, new BalemurkLeech());
        Permanent opponentCreature = addCreatureReady(player2, new BalemurkLeech());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Attack trigger's deathtouch grant wears off at end of turn")
    void attackTriggerGrantWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new FleshBurrower());
        Permanent target = addCreatureReady(player1, new BalemurkLeech());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }
}
