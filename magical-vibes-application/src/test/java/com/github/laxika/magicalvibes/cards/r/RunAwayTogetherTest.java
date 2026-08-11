package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunAwayTogetherTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two creatures controlled by different players to their owners' hands")
    void bouncesCreaturesControlledByDifferentPlayers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RunAwayTogether()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInHand(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target two creatures controlled by the same player")
    void cannotTargetCreaturesControlledBySamePlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new RunAwayTogether()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID firstId = battlefield.get(0).getId();
        UUID secondId = battlefield.get(1).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(firstId, secondId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
