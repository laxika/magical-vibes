package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({FirefrightMage.class, GoblinPiker.class, GrizzlyBears.class, Ornithopter.class})
class FirefrightMageTest extends BaseCardTest {

    @Test
    void activationRequiresDiscardingACard() {
        Permanent mage = addReady(player1, new FirefrightMage());
        Permanent target = addReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Ornithopter()));
        addActivationMana();

        harness.activateAbility(player1, indexOf(player1, mage), null, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(
                com.github.laxika.magicalvibes.model.PendingInteraction.DiscardCostChoice.class);
    }

    @Test
    void nonArtifactNonRedCreatureCannotBlock() {
        Permanent target = activateOnTarget();
        Permanent blocker = addReady(player2, new GrizzlyBears());

        target.setAttacking(true);
        beginDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact creatures and/or red creatures");
    }

    @Test
    void artifactCreatureCanBlock() {
        Permanent target = activateOnTarget();
        Permanent blocker = addReady(player2, new Ornithopter());

        target.setAttacking(true);
        beginDeclareBlockers();
        declareBlock(blocker, target);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void redCreatureCanBlock() {
        Permanent target = activateOnTarget();
        Permanent blocker = addReady(player2, new GoblinPiker());

        target.setAttacking(true);
        beginDeclareBlockers();
        declareBlock(blocker, target);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void restrictionExpiresAtEndOfTurn() {
        Permanent target = activateOnTarget();
        Permanent blocker = addReady(player2, new GrizzlyBears());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        target.setAttacking(true);
        beginDeclareBlockers();
        declareBlock(blocker, target);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void cannotActivateWithoutCardToDiscard() {
        Permanent mage = addReady(player1, new FirefrightMage());
        Permanent target = addReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, mage), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent activateOnTarget() {
        Permanent mage = addReady(player1, new FirefrightMage());
        Permanent target = addReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addActivationMana();

        harness.activateAbility(player1, indexOf(player1, mage), null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(mage.isTapped()).isTrue();
        return target;
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void beginDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
