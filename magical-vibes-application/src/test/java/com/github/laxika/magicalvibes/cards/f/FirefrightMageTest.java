package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CitanulWoodreaders;
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

@CardUsed({FirefrightMage.class, CitanulWoodreaders.class, Ornithopter.class})
class FirefrightMageTest extends BaseCardTest {

    @Test
    void activationRequiresDiscardingACard() {
        Permanent mage = addCreatureReady(player1, new FirefrightMage());
        Permanent target = addCreatureReady(player1, new CitanulWoodreaders());
        harness.setHand(player1, List.of(new CitanulWoodreaders(), new Ornithopter()));
        addActivationMana();

        harness.activateAbility(player1, indexOf(player1, mage), null, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(
                com.github.laxika.magicalvibes.model.PendingInteraction.DiscardCostChoice.class);
    }

    @Test
    void nonArtifactNonRedCreatureCannotBlock() {
        Permanent target = activateOnTarget();
        Permanent blocker = addCreatureReady(player2, new CitanulWoodreaders());

        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact creatures and/or red creatures");
    }

    @Test
    void artifactCreatureCanBlock() {
        Permanent target = activateOnTarget();
        Permanent blocker = addCreatureReady(player2, new Ornithopter());

        target.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(blocker, target);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void redCreatureCanBlock() {
        Permanent target = activateOnTarget();
        Permanent blocker = addCreatureReady(player2, new FirefrightMage());

        target.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(blocker, target);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void restrictionExpiresAtEndOfTurn() {
        Permanent target = activateOnTarget();
        Permanent blocker = addCreatureReady(player2, new CitanulWoodreaders());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        target.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(blocker, target);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void cannotActivateWithoutCardToDiscard() {
        Permanent mage = addCreatureReady(player1, new FirefrightMage());
        Permanent target = addCreatureReady(player1, new CitanulWoodreaders());
        harness.setHand(player1, List.of());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, mage), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetCreatureOpponentControls() {
        Permanent target = activateOnTarget(player2);
        Permanent blocker = addCreatureReady(player1, new CitanulWoodreaders());

        target.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> declareBlock(player1, blocker, player2, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact creatures and/or red creatures");
    }

    private Permanent activateOnTarget() {
        return activateOnTarget(player1);
    }

    private Permanent activateOnTarget(Player targetController) {
        Permanent mage = addCreatureReady(player1, new FirefrightMage());
        Permanent target = addCreatureReady(targetController, new CitanulWoodreaders());
        harness.setHand(player1, List.of(new CitanulWoodreaders()));
        addActivationMana();

        harness.activateAbility(player1, indexOf(player1, mage), null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(mage.isTapped()).isTrue();
        return target;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        declareBlock(player2, blocker, player1, attacker);
    }

    private void declareBlock(Player defendingPlayer, Permanent blocker,
                              Player attackingPlayer, Permanent attacker) {
        gs.declareBlockers(gd, defendingPlayer, List.of(new BlockerAssignment(
                indexOf(defendingPlayer, blocker), indexOf(attackingPlayer, attacker))));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
