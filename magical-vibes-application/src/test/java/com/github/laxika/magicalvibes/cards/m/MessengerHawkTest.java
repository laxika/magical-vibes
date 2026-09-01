package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MessengerHawk.class, Island.class})
class MessengerHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Clue when it enters the battlefield")
    void createsClueOnEntering() {
        harness.enterBattlefieldAndReturn(player1, new MessengerHawk());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Gets +2/+0 after its controller draws two cards")
    void gainsPowerAfterControllerDrawsTwoCards() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new MessengerHawk());
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);

        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);

        draw(player1);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's draws do not grant the power bonus")
    void opponentDrawsDoNotGrantPowerBonus() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new MessengerHawk());
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        draw(player2);
        draw(player2);

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
