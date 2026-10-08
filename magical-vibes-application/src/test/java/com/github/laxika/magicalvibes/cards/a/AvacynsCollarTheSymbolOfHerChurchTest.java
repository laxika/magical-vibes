package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HinterlandHermit;
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
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent collar = harness.addToBattlefieldAndReturn(player1,
                new AvacynsCollarTheSymbolOfHerChurch());
        addShackleMana();

        harness.activateAbility(player1, indexOf(player1, collar), null, creature.getId());
        harness.passBothPriorities();

        assertThat(collar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void shackleRejectsYourOwnCreatureAndInstantSpeedActivation() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent creature = addCreatureReady(player2, new BottleGnomes());
        Permanent collar = attachCollar(creature);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(indexOf(player2, creature))))
                .isInstanceOf(IllegalStateException.class);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

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
        Permanent creature = addCreatureReady(player2, new HinterlandHermit());
        attachCollar(creature);
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        resolveAllTriggers();


        assertThat(creature.isTransformed()).isFalse();
    }

    @Test
    void movingCollarReleasesThePreviouslyShackledCreature() {
        Permanent first = addCreatureReady(player2, new BottleGnomes());
        Permanent second = addCreatureReady(player2, new BottleGnomes());
        Permanent collar = attachCollar(first);
        addShackleMana();

        harness.activateAbility(player1, indexOf(player1, collar), null, second.getId());
        harness.passBothPriorities();

        assertThat(collar.getAttachedTo()).isEqualTo(second.getId());
        assertThatThrownBy(() -> harness.activateAbility(
                player2, indexOf(player2, second), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        harness.activateAbility(player2, indexOf(player2, first), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first);
        harness.assertLife(player2, 23);
    }

    @Test
    void targetLeavingBeforeResolutionPreservesThePreviousAttachment() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new BottleGnomes());
        Permanent collar = attachCollar(first);
        addShackleMana();

        harness.activateAbility(player1, indexOf(player1, collar), null, second.getId());
        harness.activateAbility(player2, indexOf(player2, second), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(second);
        assertThat(collar.getAttachedTo()).isEqualTo(first.getId());
        assertThatThrownBy(() -> declareAttackers(player2, List.of(indexOf(player2, first))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent attachCollar(Permanent creature) {
        Permanent collar = harness.addToBattlefieldAndReturn(player1,
                new AvacynsCollarTheSymbolOfHerChurch());
        collar.setAttachedTo(creature.getId());
        return collar;
    }

    private void addShackleMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
