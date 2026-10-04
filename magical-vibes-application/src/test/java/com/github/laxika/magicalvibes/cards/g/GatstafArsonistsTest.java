package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RushOfAdrenaline;
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

@CardUsed({GatstafArsonists.class, RushOfAdrenaline.class})
class GatstafArsonistsTest extends BaseCardTest {

    @Test
    void transformsWhenNoSpellsWereCastLastTurn() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolve(player1);

        assertThat(arsonists.isTransformed()).isTrue();
    }

    @Test
    void doesNotTransformWhenAPlayerCastOneSpellLastTurn() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(arsonists.isTransformed()).isFalse();
    }

    @Test
    void transformsBackWhenAPlayerCastTwoSpellsLastTurn() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);
        assertThat(arsonists.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeepAndResolve(player2);

        assertThat(arsonists.isTransformed()).isFalse();
    }

    @Test
    void doesNotTransformBackWhenEachPlayerCastOnlyOneSpellLastTurn() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);
        assertThat(arsonists.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        advanceToUpkeep(player2);

        assertThat(arsonists.isTransformed()).isTrue();
    }

    private Permanent addArsonists() {
        return harness.addToBattlefieldAndReturn(player1, new GatstafArsonists());
    }

    @Test
    void transformsDuringOpponentsUpkeepWhenNoSpellsWereCast() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolve(player2);

        assertThat(arsonists.isTransformed()).isTrue();
    }

    @Test
    void opponentsSpellLastTurnPreventsFrontFaceTrigger() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(arsonists.isTransformed()).isFalse();
    }

    @Test
    void spellCastInResponseDoesNotPreventTransformation() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.clear();
        harness.setHand(player1, List.of(new RushOfAdrenaline()));
        harness.addMana(player1, ManaColor.RED, 1);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.castInstant(player1, 0, arsonists.getId());
            resolveAllTriggers();
        });

        assertThat(arsonists.isTransformed()).isTrue();
    }

    @Test
    void backFaceRemainsTransformedAfterAnotherSpelllessTurn() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(arsonists.isTransformed()).isTrue();
    }

    @Test
    void backFaceTransformsDuringControllersUpkeepAfterTwoSpells() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player2);
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        advanceToUpkeepAndResolve(player1);

        assertThat(arsonists.isTransformed()).isFalse();
    }

    @Test
    void transformedCreatureRequiresTwoBlockers() {
        Permanent arsonists = addArsonists();
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);
        arsonists.setSummoningSick(false);
        addCreatureReady(player2, new GatstafArsonists());
        addCreatureReady(player2, new GatstafArsonists());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }

    private void advanceToUpkeepAndResolve(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }
}
