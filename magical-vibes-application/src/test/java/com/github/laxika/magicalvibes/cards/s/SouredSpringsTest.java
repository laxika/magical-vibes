package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SouredSprings.class)
class SouredSpringsTest extends BaseCardTest {

    @Test
    void entersTappedAndDealsDamageToTargetOpponent() {
        harness.setHand(player1, List.of(new SouredSprings()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void tappingProducesBlueMana() {
        Permanent land = addCreatureReady(player1, new SouredSprings());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        GameData gameData = harness.getGameData();

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void tappingProducesBlackMana() {
        Permanent land = addCreatureReady(player1, new SouredSprings());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        GameData gameData = harness.getGameData();

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void tappedLandCannotProduceMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SouredSprings());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void entryTriggerStillDealsDamageAfterLandLeavesBattlefield() {
        harness.setHand(player1, List.of(new SouredSprings()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());

        Permanent land = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(land.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
