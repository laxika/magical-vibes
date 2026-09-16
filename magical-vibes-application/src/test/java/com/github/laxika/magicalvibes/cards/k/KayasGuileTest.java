package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KayasGuile.class, FugitiveWizard.class, GrizzlyBears.class})
class KayasGuileTest extends BaseCardTest {

    @Test
    void sacrificesAnOpposingCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setLife(player1, 20);

        cast(new int[]{0, 3}, 1);

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void exilesOpponentsGraveyardsAndCreatesFlyingSpirit() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));

        cast(new int[]{1, 2}, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().getName().equals("Spirit")
                        && permanent.getCard().hasKeyword(Keyword.FLYING));
    }

    @Test
    void entwinePaysOneFixedCostAndResolvesAllModes() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLife(player1, 20);

        cast(new int[]{0, 1, 2, 3}, 4);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(graveyardCard, opponentCreature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().getName().equals("Spirit")
                        && permanent.getCard().hasKeyword(Keyword.FLYING));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void entwineRejectsSelectingThreeModes() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new KayasGuile()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, 4, new int[]{0, 1, 2}, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 2 modes");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(6);
    }

    private void cast(int[] modes, int colorlessMana) {
        harness.setHand(player1, List.of(new KayasGuile()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castModalInstantWithModes(player1, 0, 2, 4, modes, List.of());
        harness.passBothPriorities();
    }
}
