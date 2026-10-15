package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.b.BribersPurse;
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

@CardUsed({MarduRoughrider.class, AlpineGrizzly.class, BribersPurse.class})
class MarduRoughriderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes the chosen creature unable to block this turn")
    void attackMakesTargetUnableToBlock() {
        addCreatureReady(player1, new MarduRoughrider());
        Permanent blocker = addCreatureReady(player2, new AlpineGrizzly());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new MarduRoughrider());
        Permanent target = addCreatureReady(player1, new AlpineGrizzly());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new MarduRoughrider());
        addCreatureReady(player2, new AlpineGrizzly());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BribersPurse());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The attack trigger can target Mardu Roughrider itself")
    void canTargetItself() {
        Permanent roughrider = addCreatureReady(player1, new MarduRoughrider());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, roughrider.getId());
        resolveAllTriggers();

        assertThat(roughrider.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Other creatures can still block after the attack trigger resolves")
    void onlyChosenCreatureCannotBlock() {
        addCreatureReady(player1, new MarduRoughrider());
        Permanent target = addCreatureReady(player2, new AlpineGrizzly());
        Permanent other = addCreatureReady(player2, new AlpineGrizzly());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(other.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires when the turn ends")
    void blockingRestrictionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new MarduRoughrider());
        Permanent target = addCreatureReady(player2, new AlpineGrizzly());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }
}
