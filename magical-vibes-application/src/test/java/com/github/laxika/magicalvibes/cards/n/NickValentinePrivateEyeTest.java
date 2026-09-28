package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NickValentinePrivateEye.class, Ornithopter.class, GrizzlyBears.class, Shock.class})
class NickValentinePrivateEyeTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByArtifactCreatures() {
        Permanent nick = addAttackingNick();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
        assertThat(nick.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void canBeBlockedByNonArtifactCreatures() {
        addAttackingNick();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginBlockerDeclaration();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void artifactCreatureDeathOffersInvestigation() {
        harness.addToBattlefield(player1, new NickValentinePrivateEye());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        killWithShock(ornithopter, player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void nonArtifactCreatureDeathDoesNotTriggerInvestigation() {
        harness.addToBattlefield(player1, new NickValentinePrivateEye());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(bears, player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void NickValentinesDeathOffersInvestigation() {
        Permanent nick = harness.addToBattlefieldAndReturn(player1, new NickValentinePrivateEye());

        killWithShock(nick, player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void decliningInvestigationCreatesNoClue() {
        harness.addToBattlefield(player1, new NickValentinePrivateEye());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        killWithShock(ornithopter, player2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private Permanent addAttackingNick() {
        Permanent nick = harness.addToBattlefieldAndReturn(player1, new NickValentinePrivateEye());
        nick.setSummoningSick(false);
        nick.setAttacking(true);
        return nick;
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void killWithShock(Permanent target, com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
