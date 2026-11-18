package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlesongBerserker.class, SavannahLions.class})
class BattlesongBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger only targets a creature you control")
    void attackTriggerTargetsOwnCreature() {
        addReadyBerserker(player1);
        Permanent ownCreature = addCreatureReady(player1, new SavannahLions());
        Permanent opposingCreature = addCreatureReady(player2, new SavannahLions());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opposingCreature.getId());
    }

    @Test
    @DisplayName("Attack trigger gives the target +1/+0 and menace until end of turn")
    void attackTriggerBoostsAndGrantsMenace() {
        addReadyBerserker(player1);
        Permanent target = addCreatureReady(player1, new SavannahLions());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The boost and menace wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        addReadyBerserker(player1);
        Permanent target = addCreatureReady(player1, new SavannahLions());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Attacking with another creature triggers a summoning-sick Berserker")
    void triggersWithoutBerserkerAttacking() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new BattlesongBerserker());
        Permanent attacker = addCreatureReady(player1, new SavannahLions());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(berserker.getId(), attacker.getId());
        harness.handlePermanentChosen(player1, berserker.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, berserker, Keyword.MENACE)).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Attacking with multiple other creatures triggers only once")
    void triggersOnceForMultipleAttackers() {
        addReadyBerserker(player1).tap();
        Permanent firstAttacker = addCreatureReady(player1, new SavannahLions());
        addCreatureReady(player1, new SavannahLions());

        declareAttackers(List.of(1, 2));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstAttacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, firstAttacker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, firstAttacker, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Berserker can target itself when it attacks")
    void canTargetItself() {
        Permanent berserker = addReadyBerserker(player1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, berserker.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, berserker, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("An opponent attacking does not trigger Berserker")
    void doesNotTriggerForOpponentAttack() {
        Permanent berserker = addReadyBerserker(player1);
        addCreatureReady(player2, new SavannahLions());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(PermanentGrants.powerBoostOf(gd, berserker)).isZero();
        assertThat(gqs.hasKeyword(gd, berserker, Keyword.MENACE)).isFalse();
    }

    private Permanent addReadyBerserker(Player player) {
        return addCreatureReady(player, new BattlesongBerserker());
    }
}
