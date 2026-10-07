package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimelineCuller.class})
class TimelineCullerTest extends BaseCardTest {

    @Test
    @DisplayName("Warp from hand pays {B} and 2 life, then exiles at the next end step")
    void warpFromHandPaysManaAndLifeAndExilesAtNextEndStep() {
        TimelineCuller culler = new TimelineCuller();
        harness.setHand(player1, List.of(culler));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.assertLife(player1, 18);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Timeline Culler");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(culler.getId())).isNotNull();
    }

    @Test
    @DisplayName("Warp from the graveyard pays {B} and 2 life, then exiles at the next end step")
    void warpFromGraveyardPaysManaAndLifeAndExilesAtNextEndStep() {
        TimelineCuller culler = new TimelineCuller();
        harness.setGraveyard(player1, List.of(culler));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        harness.assertLife(player1, 18);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Timeline Culler");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(culler.getId())).isNotNull();
    }

    @Test
    void graveyardWarpCanAttackTheTurnItEnters() {
        harness.setGraveyard(player1, List.of(new TimelineCuller()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    void normalCastDoesNotPayLifeOrExileAtEndStep() {
        TimelineCuller culler = new TimelineCuller();
        harness.setHand(player1, List.of(culler));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Timeline Culler");
        assertThat(gd.findExiledCard(culler.getId())).isNull();
    }

    @Test
    void cannotWarpFromHandWithInsufficientLife() {
        harness.setHand(player1, List.of(new TimelineCuller()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        harness.assertInHand(player1, "Timeline Culler");
        harness.assertNotOnBattlefield(player1, "Timeline Culler");
    }

    @Test
    void cannotWarpFromGraveyardWithInsufficientLife() {
        harness.setGraveyard(player1, List.of(new TimelineCuller()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        harness.assertInGraveyard(player1, "Timeline Culler");
        harness.assertNotOnBattlefield(player1, "Timeline Culler");
    }

    @Test
    void graveyardWarpCanBeRecastFromExileOnALaterTurnForNormalCost() {
        TimelineCuller culler = new TimelineCuller();
        harness.setGraveyard(player1, List.of(culler));
        harness.setLibrary(player1, List.of(new TimelineCuller(), new TimelineCuller()));
        harness.setLibrary(player2, List.of(new TimelineCuller(), new TimelineCuller()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(culler.getId())).isNotNull();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, culler.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Timeline Culler");
        assertThat(gd.findExiledCard(culler.getId())).isNull();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Timeline Culler");
        assertThat(gd.findExiledCard(culler.getId())).isNull();
    }
}
