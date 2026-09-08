package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgainstAllOddsTest extends BaseCardTest {

    @Test
    @DisplayName("Flicker mode exiles and returns an artifact or creature you control")
    void flickersControlledArtifactOrCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgainstAllOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        var bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        gs.playCard(gd, player1, 0, -1, null, null, List.of(bearsId), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
    }

    @Test
    @DisplayName("Graveyard mode returns a qualifying artifact or creature")
    void returnsQualifyingCardFromGraveyard() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new AgainstAllOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, -2, creature.getId(), null, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Both modes resolve with their independent targets")
    void resolvesBothModes() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgainstAllOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        var bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        gs.playCard(gd, player1, 0, -3, creature.getId(), null,
                List.of(bearsId), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
    }

    @Test
    @DisplayName("Targets must match the selected mode")
    void rejectsIllegalTargets() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgainstAllOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, -2, instant.getId(), null, List.of(), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, -1, null, null,
                List.of(harness.getPermanentId(player2, "Grizzly Bears")), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
