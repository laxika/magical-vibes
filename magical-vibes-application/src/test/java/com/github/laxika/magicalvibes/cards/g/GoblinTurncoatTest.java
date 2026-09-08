package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FarrelitePriest;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTurncoat.class, RagingGoblin.class, FarrelitePriest.class})
class GoblinTurncoatTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another Goblin regenerates Goblin Turncoat")
    void sacrificesAnotherGoblinAndRegeneratesItself() {
        Permanent turncoat = harness.addToBattlefieldAndReturn(player1, new GoblinTurncoat());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(turncoat).doesNotContain(goblin);
        assertThat(turncoat.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Goblin Turncoat may sacrifice itself")
    void canSacrificeItself() {
        Permanent turncoat = harness.addToBattlefieldAndReturn(player1, new GoblinTurncoat());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(turncoat);
        harness.assertInGraveyard(player1, "Goblin Turncoat");
    }

    @Test
    @DisplayName("Sacrifice cost only accepts Goblins")
    void sacrificeCostOnlyAcceptsGoblins() {
        harness.addToBattlefield(player1, new GoblinTurncoat());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new FarrelitePriest());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonGoblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonGoblin).doesNotContain(goblin);
    }
}
