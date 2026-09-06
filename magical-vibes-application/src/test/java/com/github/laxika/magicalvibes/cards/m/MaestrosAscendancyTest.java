package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaestrosAscendancy.class, GrizzlyBears.class, Shock.class})
class MaestrosAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("Casts an instant from the graveyard by sacrificing a creature and exiles it")
    void castsInstantBySacrificingCreatureAndExilesIt() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        harness.castFromGraveyardWithSacrifices(player1, 0, player2.getId(), List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getCard().getId());
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
    }

    @Test
    @DisplayName("Requires a creature and allows only one graveyard cast each turn")
    void requiresCreatureAndIsLimitedToOneCastPerTurn() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase();

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, player2.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId())).containsExactly(0, 1);

        harness.castFromGraveyardWithSacrifices(
                player1, 0, player2.getId(), List.of(firstCreature.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(
                player1, 0, player2.getId(), List.of(secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not cast a permanent card from the graveyard")
    void onlyCastsInstantsAndSorceries() {
        harness.addToBattlefield(player1, new MaestrosAscendancy());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
