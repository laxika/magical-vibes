package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnstoppableOgre.class, BambooGroveArcher.class})
class UnstoppableOgreTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes the targeted creature unable to block this turn")
    void etbMakesTargetUnableToBlock() {
        Permanent blocker = addCreatureReady(player2, new BambooGroveArcher());
        harness.setHand(player1, List.of(new UnstoppableOgre()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, 0, blocker.getId());
        resolveAllTriggers();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The restriction wears off at end of turn")
    void restrictionWearsOffAtEndOfTurn() {
        Permanent blocker = addCreatureReady(player2, new BambooGroveArcher());
        harness.setHand(player1, List.of(new UnstoppableOgre()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, 0, blocker.getId());
        resolveAllTriggers();
        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The trigger can target a friendly creature and leaves other creatures unaffected")
    void canTargetFriendlyCreature() {
        Permanent target = addCreatureReady(player1, new BambooGroveArcher());
        Permanent other = addCreatureReady(player2, new BambooGroveArcher());
        harness.setHand(player1, List.of(new UnstoppableOgre()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        assertThat(findPermanent(player1, "Unstoppable Ogre").isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The targeted creature cannot be declared as a blocker")
    void targetCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new UnstoppableOgre());
        Permanent blocker = addCreatureReady(player2, new BambooGroveArcher());
        harness.setHand(player1, List.of(new UnstoppableOgre()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, blocker.getId());
        resolveAllTriggers();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("On an empty battlefield the entry trigger can target the Ogre itself")
    void targetsItselfOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new UnstoppableOgre()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent ogre = findPermanent(player1, "Unstoppable Ogre");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, ogre.getId());
        resolveAllTriggers();

        assertThat(ogre.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entry trigger resolves even after the Ogre leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent target = addCreatureReady(player2, new BambooGroveArcher());
        harness.setHand(player1, List.of(new UnstoppableOgre()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent ogre = findPermanent(player1, "Unstoppable Ogre");
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(ogre);
        gd.playerGraveyards.get(player1.getId()).add(ogre.getCard());
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves before resolution does not affect another creature")
    void departedTargetDoesNotRestrictOtherCreatures() {
        Permanent target = addCreatureReady(player2, new BambooGroveArcher());
        Permanent other = addCreatureReady(player2, new BambooGroveArcher());
        harness.setHand(player1, List.of(new UnstoppableOgre()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        resolveAllTriggers();

        assertThat(other.isCantBlockThisTurn()).isFalse();
        assertThat(findPermanent(player1, "Unstoppable Ogre").isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
