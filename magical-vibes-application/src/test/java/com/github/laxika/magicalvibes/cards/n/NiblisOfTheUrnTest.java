package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.s.SomberwaldDryad;
import com.github.laxika.magicalvibes.cards.h.HeavyMattock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NiblisOfTheUrn.class, SomberwaldDryad.class, HeavyMattock.class})
class NiblisOfTheUrnTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues attack trigger for creature target selection")
    void attackingQueuesTargetSelection() {
        addReadyNiblis(player1);
        addCreatureReady(player2, new SomberwaldDryad());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Resolving attack trigger presents may ability choice")
    void resolvingAttackTriggerPresentsMayChoice() {
        addReadyNiblis(player1);
        Permanent dryad = addCreatureReady(player2, new SomberwaldDryad());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, dryad.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting attack may taps target opponent creature")
    void acceptingMayTapsOpponentCreature() {
        addReadyNiblis(player1);
        Permanent dryad = addCreatureReady(player2, new SomberwaldDryad());

        attackChooseTargetAndAccept(dryad);

        assertThat(dryad.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting attack may can tap own creature")
    void acceptingMayCanTapOwnCreature() {
        addReadyNiblis(player1);
        Permanent dryad = addCreatureReady(player1, new SomberwaldDryad());

        attackChooseTargetAndAccept(dryad);

        assertThat(dryad.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining attack may leaves target creature untapped")
    void decliningMayLeavesTargetUntapped() {
        addReadyNiblis(player1);
        Permanent dryad = addCreatureReady(player2, new SomberwaldDryad());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, dryad.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(dryad.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger rejects noncreature targets")
    void attackTriggerRejectsNoncreatureTargets() {
        addReadyNiblis(player1);
        Permanent mattock = harness.addToBattlefieldAndReturn(player2, new HeavyMattock());
        addCreatureReady(player2, new SomberwaldDryad());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, mattock.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void attackChooseTargetAndAccept(Permanent target) {
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private Permanent addReadyNiblis(Player player) {
        return addCreatureReady(player, new NiblisOfTheUrn());
    }

    @Test
    @DisplayName("Accepting attack may can target and keep Niblis tapped")
    void acceptingMayCanTargetSelf() {
        Permanent niblis = addReadyNiblis(player1);

        attackChooseTargetAndAccept(niblis);

        assertThat(niblis.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attack trigger resolves after its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent niblis = addReadyNiblis(player1);
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(niblis);
        gd.playerGraveyards.get(player1.getId()).add(niblis.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attack trigger does not resolve when its target leaves the battlefield")
    void triggerDoesNotResolveWithoutTarget() {
        addReadyNiblis(player1);
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).isEmpty();
    }
}
