package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrganGrinder.class, GrizzlyBears.class})
class OrganGrinderTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles three graveyard cards and makes a target player lose 3 life")
    void exilesThreeCardsAndMakesTargetPlayerLoseLife() {
        addReadyOrganGrinder(player1);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetYourself() {
        addReadyOrganGrinder(player1);
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot activate without three cards in the graveyard")
    void cannotActivateWithoutThreeGraveyardCards() {
        addReadyOrganGrinder(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyOrganGrinder(Player player) {
        Permanent permanent = new Permanent(new OrganGrinder());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
