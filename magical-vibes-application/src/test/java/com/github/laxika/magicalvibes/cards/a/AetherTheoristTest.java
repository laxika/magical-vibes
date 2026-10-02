package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherTheorist.class, Forest.class})
class AetherTheoristTest extends BaseCardTest {

    @Test
    void entersWithThreeEnergyCounters() {
        harness.castFromHand(player1, new AetherTheorist(), "{1}{U}");
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void paysEnergyAndTapsToScryOne() {
        Permanent theorist = addCreatureReady(player1, new AetherTheorist());
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(theorist.isTapped()).isTrue();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void cannotActivateWithoutEnergy() {
        addCreatureReady(player1, new AetherTheorist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one energy counter");
    }

    @Test
    void energyIsPaidBeforeResolutionAndLastEnergyCanBeSpent() {
        Permanent theorist = addCreatureReady(player1, new AetherTheorist());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        Forest top = new Forest();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(top, next));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(theorist.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canKeepTheTopCardWhenScrying() {
        addCreatureReady(player1, new AetherTheorist());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        Forest top = new Forest();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(top, next));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canScryWithAnEmptyLibrary() {
        Permanent theorist = addCreatureReady(player1, new AetherTheorist());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(theorist.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new AetherTheorist());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent theorist = addCreatureReady(player1, new AetherTheorist());
        theorist.setTapped(true);
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }
}
