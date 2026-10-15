package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SokkaLateralStrategist.class, OtterPenguin.class, Forest.class})
class SokkaLateralStrategistTest extends BaseCardTest {

    @Test
    void attackingWithAnotherCreatureDrawsACard() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        addReadySokka();
        addCreatureReady(player1, new OtterPenguin());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void attackingAloneDoesNotDraw() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        addReadySokka();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void attackingWithoutSokkaDoesNotTriggerItsAbility() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        addReadySokka();
        addCreatureReady(player1, new OtterPenguin());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void attackingWithMultipleCompanionsDrawsOnlyOneCard() {
        Forest drawn = new Forest();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(drawn, remaining));
        harness.setHand(player1, List.of());
        addReadySokka();
        addCreatureReady(player1, new OtterPenguin());
        addCreatureReady(player1, new OtterPenguin());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    void stillDrawsAfterBothAttackersLeaveTheBattlefield() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        Permanent sokka = addReadySokka();
        Permanent companion = addCreatureReady(player1, new OtterPenguin());

        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, sokka);
            harness.getPermanentRemovalService().removePermanentToHand(gd, companion);
        });
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(sokka.getCard(), companion.getCard(), drawn);
    }
    private Permanent addReadySokka() {
        return addCreatureReady(player1, new SokkaLateralStrategist());
    }
}
