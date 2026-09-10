package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KessigWolfrider.class, GrizzlyBears.class})
class KessigWolfriderTest extends BaseCardTest {

    @Test
    void exilesThreeGraveyardCardsAndCreatesWolfToken() {
        Permanent wolfrider = addReadyWolfrider(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolfrider.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);

        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().getPower()).isEqualTo(3);
        assertThat(wolf.getCard().getToughness()).isEqualTo(2);
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
    }

    @Test
    void cannotActivateWithoutThreeCardsInGraveyard() {
        Permanent wolfrider = addReadyWolfrider(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wolfrider.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Wolf")).isZero();
    }

    private Permanent addReadyWolfrider(Player player) {
        Permanent wolfrider = harness.addToBattlefieldAndReturn(player, new KessigWolfrider());
        wolfrider.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return wolfrider;
    }
}
