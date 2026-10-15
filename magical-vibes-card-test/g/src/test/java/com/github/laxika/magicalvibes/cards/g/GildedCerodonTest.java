package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.cards.d.DesertOfTheIndomitable;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GildedCerodon.class, DesertOfTheIndomitable.class, FrilledSandwalla.class})
class GildedCerodonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with a Desert on the battlefield makes the target creature unable to block")
    void attackWithDesertControlledMakesTargetCantBlock() {
        addCreatureReady(player1, new GildedCerodon());
        harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        Permanent blocker = addCreatureReady(player2, new FrilledSandwalla());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities(); // resolve the attack trigger

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Attacking with a Desert card in the graveyard makes the target creature unable to block")
    void attackWithDesertInGraveyardMakesTargetCantBlock() {
        addCreatureReady(player1, new GildedCerodon());
        harness.setGraveyard(player1, List.of(new DesertOfTheIndomitable()));
        Permanent blocker = addCreatureReady(player2, new FrilledSandwalla());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities(); // resolve the attack trigger

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Without any Desert the intervening-if fails â€” the target creature is left able to block")
    void attackWithoutDesertDoesNotRestrictBlocking() {
        addCreatureReady(player1, new GildedCerodon());
        Permanent blocker = addCreatureReady(player2, new FrilledSandwalla());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void opponentsDesertsDoNotEnableTrigger() {
        addCreatureReady(player1, new GildedCerodon());
        harness.addToBattlefield(player2, new DesertOfTheIndomitable());
        harness.setGraveyard(player2, List.of(new DesertOfTheIndomitable()));
        Permanent blocker = addCreatureReady(player2, new FrilledSandwalla());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void losingLastDesertBeforeResolutionStopsEffect() {
        addCreatureReady(player1, new GildedCerodon());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new DesertOfTheIndomitable());
        Permanent blocker = addCreatureReady(player2, new FrilledSandwalla());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(desert);
        harness.setHand(player1, List.of(desert.getCard()));
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void losingGraveyardDesertBeforeResolutionStopsEffect() {
        addCreatureReady(player1, new GildedCerodon());
        harness.setGraveyard(player1, List.of(new DesertOfTheIndomitable()));
        Permanent blocker = addCreatureReady(player2, new FrilledSandwalla());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void desertMovingToGraveyardBeforeResolutionStillEnablesEffect() {
        addCreatureReady(player1, new GildedCerodon());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new DesertOfTheIndomitable());
        Permanent blocker = addCreatureReady(player2, new FrilledSandwalla());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(desert);
        harness.setGraveyard(player1, List.of(desert.getCard()));
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void abilityCanTargetCreatureControlledByAttacker() {
        addCreatureReady(player1, new GildedCerodon());
        harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        Permanent friendlyCreature = addCreatureReady(player1, new FrilledSandwalla());
        addCreatureReady(player2, new FrilledSandwalla());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, friendlyCreature.getId());
        harness.passBothPriorities();

        assertThat(friendlyCreature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void removingCerodonDoesNotStopItsTriggeredAbility() {
        Permanent cerodon = addCreatureReady(player1, new GildedCerodon());
        harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        Permanent blocker = addCreatureReady(player2, new FrilledSandwalla());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(cerodon);
        harness.setGraveyard(player1, List.of(cerodon.getCard()));
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void targetedCreatureCannotBeDeclaredAsBlocker() {
        addCreatureReady(player1, new GildedCerodon());
        harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        Permanent blocker = addCreatureReady(player2, new FrilledSandwalla());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void blockingRestrictionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new GildedCerodon());
        harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        Permanent blocker = addCreatureReady(player2, new FrilledSandwalla());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }
}
