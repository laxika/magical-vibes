package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PlatedKilnbeast;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        BondedHerdbeast.class,
        PlatedKilnbeast.class
})
class BondedHerdbeastTest extends BaseCardTest {

    @Test
    void transformsByPayingRedMana() {
        Permanent herdbeast = addHerdbeast();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(herdbeast.isTransformed()).isTrue();
        assertThat(herdbeast.getCard()).isInstanceOf(PlatedKilnbeast.class);
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        Permanent herdbeast = addHerdbeast();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(herdbeast.isTransformed()).isTrue();
        assertThat(herdbeast.getCard()).isInstanceOf(PlatedKilnbeast.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void cannotTransformDuringCombat() {
        Permanent herdbeast = addHerdbeast();
        prepareMainPhase();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(herdbeast.isTransformed()).isFalse();
    }

    @Test
    void cannotTransformDuringOpponentsMainPhase() {
        Permanent herdbeast = addHerdbeast();
        prepareMainPhase();
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(herdbeast.isTransformed()).isFalse();
    }

    @Test
    void cannotActivateAgainWhileTransformAbilityIsOnStack() {
        Permanent herdbeast = addHerdbeast();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThat(herdbeast.isTransformed()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.passBothPriorities();
        assertThat(herdbeast.isTransformed()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void lifePaymentDoesNotReplaceGenericMana() {
        Permanent herdbeast = addHerdbeast();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(herdbeast.isTransformed()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void transformedCreatureCannotBeBlockedByOneCreature() {
        Permanent herdbeast = addHerdbeast();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        herdbeast.setSummoningSick(false);
        addCreatureReady(player2, new BondedHerdbeast());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void transformedCreatureCanBeBlockedByTwoCreatures() {
        Permanent herdbeast = addHerdbeast();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        herdbeast.setSummoningSick(false);
        Permanent firstBlocker = addCreatureReady(player2, new BondedHerdbeast());
        Permanent secondBlocker = addCreatureReady(player2, new BondedHerdbeast());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    private Permanent addHerdbeast() {
        return harness.addToBattlefieldAndReturn(player1, new BondedHerdbeast());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
