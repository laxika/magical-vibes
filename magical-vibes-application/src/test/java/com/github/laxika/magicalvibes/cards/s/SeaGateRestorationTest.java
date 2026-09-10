package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeaGateRestoration.class, SeaGateReborn.class, Forest.class})
class SeaGateRestorationTest extends BaseCardTest {

    @Test
    void restorationDrawsHandSizePlusOneAndRemovesMaximumHandSize() {
        harness.setHand(player1, List.of(new SeaGateRestoration(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
    }

    @Test
    void rebornEntersUntappedWhenThreeLifeIsPaidAndProducesBlueMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SeaGateRestoration()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(SeaGateReborn.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(land.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void rebornEntersTappedWhenLifePaymentIsDeclined() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SeaGateRestoration()));

        gs.playCard(gd, player1, 0, 1, null, null);

        harness.handleMayAbilityChosen(player1, false);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
    }
}
