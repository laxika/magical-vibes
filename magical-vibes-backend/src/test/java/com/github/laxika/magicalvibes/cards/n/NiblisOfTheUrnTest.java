package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NiblisOfTheUrnTest extends BaseCardTest {

    @Test
    @DisplayName("Has optional ON_ATTACK TapTargetPermanentEffect")
    void hasOptionalAttackTapEffect() {
        NiblisOfTheUrn card = new NiblisOfTheUrn();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(TapTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Attacking queues attack trigger for creature target selection")
    void attackingQueuesTargetSelection() {
        addReadyNiblis(player1);
        addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Resolving attack trigger presents may ability choice")
    void resolvingAttackTriggerPresentsMayChoice() {
        addReadyNiblis(player1);
        Permanent bears = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting attack may taps target opponent creature")
    void acceptingMayTapsOpponentCreature() {
        addReadyNiblis(player1);
        Permanent bears = addReadyCreature(player2);

        attackChooseTargetAndAccept(bears);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting attack may can tap own creature")
    void acceptingMayCanTapOwnCreature() {
        addReadyNiblis(player1);
        Permanent bears = addReadyCreature(player1);

        attackChooseTargetAndAccept(bears);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining attack may leaves target creature untapped")
    void decliningMayLeavesTargetUntapped() {
        addReadyNiblis(player1);
        Permanent bears = addReadyCreature(player2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger rejects noncreature targets")
    void attackTriggerRejectsNoncreatureTargets() {
        addReadyNiblis(player1);
        harness.addToBattlefield(player2, new Telepathy());
        Permanent telepathy = gd.playerBattlefields.get(player2.getId()).getFirst();
        addReadyCreature(player2);

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, telepathy.getId()))
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
        Permanent perm = new Permanent(new NiblisOfTheUrn());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Accepting attack may can target and keep Niblis tapped")
    void acceptingMayCanTargetSelf() {
        Permanent niblis = addReadyNiblis(player1);

        attackChooseTargetAndAccept(niblis);

        assertThat(niblis.isTapped()).isTrue();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
