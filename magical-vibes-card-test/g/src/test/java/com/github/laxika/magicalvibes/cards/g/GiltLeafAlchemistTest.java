package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiltLeafAlchemist.class, LlanowarElves.class, Forest.class})
class GiltLeafAlchemistTest extends BaseCardTest {

    @Test
    void conjuresForestWithTwoElfCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new LlanowarElves()));
        Permanent alchemist = addCreatureReady(player1, new GiltLeafAlchemist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(alchemist.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithFewerThanTwoElfCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        addCreatureReady(player1, new GiltLeafAlchemist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonElfCardsDoNotCountTowardActivationRequirement() {
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new Forest(), new Forest()));
        Permanent alchemist = addCreatureReady(player1, new GiltLeafAlchemist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(alchemist.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void opposingGraveyardDoesNotSatisfyActivationRequirement() {
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setGraveyard(player2, List.of(new LlanowarElves(), new LlanowarElves()));
        Permanent alchemist = addCreatureReady(player1, new GiltLeafAlchemist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(alchemist.isTapped()).isFalse();
    }

    @Test
    void graveyardRequirementIsNotCheckedAgainAtResolution() {
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new LlanowarElves()));
        addCreatureReady(player1, new GiltLeafAlchemist());

        harness.activateAbility(player1, 0, null, null);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Forest");
        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isFalse();
        assertThat(forest.getCard().isToken()).isFalse();
        assertThat(forest.getCard().getOwnerId()).isEqualTo(player1.getId());
    }

    @Test
    void canActivateWithMoreThanTwoElfCardsWithoutConsumingThem() {
        harness.setGraveyard(player1, List.of(
                new LlanowarElves(), new LlanowarElves(), new GiltLeafAlchemist()));
        addCreatureReady(player1, new GiltLeafAlchemist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void summoningSickAlchemistCannotPayTapCost() {
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new LlanowarElves()));
        Permanent alchemist = addCreatureReady(player1, new GiltLeafAlchemist());
        alchemist.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(alchemist.isTapped()).isFalse();
    }
}
