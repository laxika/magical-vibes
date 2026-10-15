package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathlessKnight.class})
class DeathlessKnightTest extends BaseCardTest {

    @Test
    void returnsFromGraveyardWhenControllerGainsLife() {
        DeathlessKnight knight = new DeathlessKnight();
        harness.setGraveyard(player1, List.of(knight));

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        assertThat(gd.stack).singleElement()
                .extracting(entry -> entry.getEntryType())
                .isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(knight);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(knight);
    }

    @Test
    void triggersOnlyForFirstLifeGainEachTurn() {
        DeathlessKnight knight = new DeathlessKnight();
        harness.setGraveyard(player1, List.of(knight));

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        });

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(knight);
    }

    @Test
    void opponentLifeGainDoesNotTriggerIt() {
        DeathlessKnight knight = new DeathlessKnight();
        harness.setGraveyard(player1, List.of(knight));

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(knight);
    }

    @Test
    void doesNotTriggerIfOwnerAlreadyGainedLifeBeforeItEnteredGraveyard() {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        DeathlessKnight knight = new DeathlessKnight();
        harness.setGraveyard(player1, List.of(knight));

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(knight);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(knight);
    }

    @Test
    void firstLifeGainReturnsEveryCopyAlreadyInGraveyard() {
        DeathlessKnight first = new DeathlessKnight();
        DeathlessKnight second = new DeathlessKnight();
        harness.setGraveyard(player1, List.of(first, second));

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    void cannotReturnSourceThatWasExiledBeforeTriggerResolved() {
        DeathlessKnight knight = new DeathlessKnight();
        harness.setGraveyard(player1, List.of(knight));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(knight));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(knight);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(knight.getId()));
    }

    @Test
    void gainingZeroLifeDoesNotConsumeFirstLifeGain() {
        DeathlessKnight knight = new DeathlessKnight();
        harness.setGraveyard(player1, List.of(knight));

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0));
        assertThat(gd.stack).isEmpty();
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(knight);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(knight);
    }
}
