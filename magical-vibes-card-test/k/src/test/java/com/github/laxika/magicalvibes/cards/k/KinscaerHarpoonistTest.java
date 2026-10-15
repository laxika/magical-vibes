package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KinscaerHarpoonist.class, Island.class, DevotedDruid.class})
class KinscaerHarpoonistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues attack trigger for target selection")
    void attackQueuesTargetSelection() {
        addCreatureReady(player1, new KinscaerHarpoonist());
        addCreatureReady(player2, new KinscaerHarpoonist());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Attack trigger only offers creatures as targets")
    void targetFilterOnlyOffersCreatures() {
        addCreatureReady(player1, new KinscaerHarpoonist());
        Permanent ownCreature = addCreatureReady(player1, new KinscaerHarpoonist());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(island.getId());
    }

    @Test
    @DisplayName("Accepting the may ability makes the target creature lose flying")
    void acceptingRemovesFlying() {
        addCreatureReady(player1, new KinscaerHarpoonist());
        Permanent hawk = addCreatureReady(player2, new KinscaerHarpoonist());

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isTrue();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, hawk.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Declining the may ability leaves flying intact")
    void decliningKeepsFlying() {
        addCreatureReady(player1, new KinscaerHarpoonist());
        Permanent hawk = addCreatureReady(player2, new KinscaerHarpoonist());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, hawk.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The attacking Harpoonist can target itself")
    void canRemoveItsOwnFlying() {
        Permanent attacker = addCreatureReady(player1, new KinscaerHarpoonist());

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A creature without flying is still a legal target")
    void canTargetCreatureWithoutFlying() {
        Permanent attacker = addCreatureReady(player1, new KinscaerHarpoonist());
        Permanent druid = addCreatureReady(player2, new DevotedDruid());

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(druid.getId());
        harness.handlePermanentChosen(player1, druid.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, druid, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger resolves after its source leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent attacker = addCreatureReady(player1, new KinscaerHarpoonist());
        Permanent target = addCreatureReady(player2, new KinscaerHarpoonist());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, attacker);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("The trigger does not resolve when its only target leaves the battlefield")
    void triggerDoesNotResolveForMissingTarget() {
        Permanent attacker = addCreatureReady(player1, new KinscaerHarpoonist());
        Permanent target = addCreatureReady(player2, new KinscaerHarpoonist());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying is restored at end of turn")
    void flyingWearsOff() {
        addCreatureReady(player1, new KinscaerHarpoonist());
        Permanent hawk = addCreatureReady(player2, new KinscaerHarpoonist());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, hawk.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isFalse();

        // Clear the pending blocker declaration so the turn can advance to cleanup.
        resolveCombat();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isTrue();
    }
}
