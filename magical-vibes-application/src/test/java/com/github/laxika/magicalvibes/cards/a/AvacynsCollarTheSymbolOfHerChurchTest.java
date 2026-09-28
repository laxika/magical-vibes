package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HinterlandHermit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvacynsCollarTheSymbolOfHerChurch.class, BottleGnomes.class,
        GrizzlyBears.class, HinterlandHermit.class})
class AvacynsCollarTheSymbolOfHerChurchTest extends BaseCardTest {

    @Test
    void shackleAttachesToAnOpponentCreature() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());
        Permanent collar = harness.addToBattlefieldAndReturn(player1,
                new AvacynsCollarTheSymbolOfHerChurch());
        addShackleMana();

        harness.activateAbility(player1, indexOf(player1, collar), null, creature.getId());
        harness.passBothPriorities();

        assertThat(collar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void shackleRejectsYourOwnCreatureAndInstantSpeedActivation() {
        Permanent ownCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentCreature = addReadyCreature(player2, new GrizzlyBears());
        Permanent collar = harness.addToBattlefieldAndReturn(player1,
                new AvacynsCollarTheSymbolOfHerChurch());
        addShackleMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, collar), null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, collar), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shackledCreatureCannotAttackBlockOrActivateAbilities() {
        Permanent creature = addReadyCreature(player2, new BottleGnomes());
        Permanent collar = attachCollar(creature);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(indexOf(player2, creature))))
                .isInstanceOf(IllegalStateException.class);

        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, creature), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(
                player2, indexOf(player2, creature), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        assertThat(collar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void shackledCreatureCannotTransform() {
        Permanent creature = addReadyCreature(player2, new HinterlandHermit());
        attachCollar(creature);
        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTransformed()).isFalse();
    }

    private Permanent attachCollar(Permanent creature) {
        Permanent collar = harness.addToBattlefieldAndReturn(player1,
                new AvacynsCollarTheSymbolOfHerChurch());
        collar.setAttachedTo(creature.getId());
        return collar;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void addShackleMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
