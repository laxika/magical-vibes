package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EchoingDeeps.class, Forest.class})
class EchoingDeepsTest extends BaseCardTest {

    @Test
    void entersUntappedWhenThereAreNoLandCardsInGraveyards() {
        harness.setHand(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayEnterTappedAsCopyOfLandCardInAnyGraveyard() {
        Card forest = new Forest();
        harness.setHand(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player2, List.of(forest));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(entered.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(forest);

        entered.untap();
        harness.activateAbility(player1, 0, 0, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void decliningCopyChoiceMakesItEnterUntapped() {
        harness.setHand(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player2, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }
}
