package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunderTheGateway.class, GloriousAnthem.class})
class SunderTheGatewayTest extends BaseCardTest {

    @Test
    void destroysOpponentEnchantmentAndIncubates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        cast(player1, List.of(new SunderTheGateway()), target.getId(), 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gameData.playerGraveyards.get(player2.getId())).contains(target.getCard());

        Permanent incubator = findIncubator(player1);
        assertThat(incubator).isNotNull();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void secondModeIncubatesThenTransformsChosenToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new SunderTheGateway(), new SunderTheGateway()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        Permanent incubator = findIncubator(player1);
        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, incubator.getId());
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(2);
    }

    @Test
    void firstModeCannotTargetPermanentYouControl() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new SunderTheGateway()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(com.github.laxika.magicalvibes.model.Player player, List<com.github.laxika.magicalvibes.model.Card> cards,
                      java.util.UUID targetId, int mode) {
        harness.setHand(player, cards);
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castSorcery(player, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private Permanent findIncubator(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElse(null);
    }
}
