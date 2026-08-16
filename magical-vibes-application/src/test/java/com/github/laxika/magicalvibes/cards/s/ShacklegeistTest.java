package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InnocenceKami;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShacklegeistTest extends BaseCardTest {

    @Test
    void tapsTwoSpiritsIncludingItselfAndTapsOpponentCreature() {
        Permanent shacklegeist = addReady(player1, new Shacklegeist());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new InnocenceKami());
        Permanent extraSpirit = harness.addToBattlefieldAndReturn(player1, new InnocenceKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, shacklegeist.getId());
        harness.handlePermanentChosen(player1, spirit.getId());
        harness.passBothPriorities();

        assertThat(shacklegeist.isTapped()).isTrue();
        assertThat(spirit.isTapped()).isTrue();
        assertThat(extraSpirit.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotTargetCreatureYouControl() {
        addReady(player1, new Shacklegeist());
        harness.addToBattlefield(player1, new InnocenceKami());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockFlyingCreature() {
        Permanent shacklegeist = addReady(player2, new Shacklegeist());
        Permanent attacker = addReady(player1, new AirElemental());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(shacklegeist.isBlocking()).isTrue();
    }

    @Test
    void cannotBlockGroundCreature() {
        Permanent shacklegeist = addReady(player2, new Shacklegeist());
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
