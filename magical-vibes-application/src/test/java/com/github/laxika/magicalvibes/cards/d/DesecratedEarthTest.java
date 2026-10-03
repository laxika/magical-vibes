package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesecratedEarth.class, Forest.class, Island.class, KrakenHatchling.class, ObstinateBaloth.class})
class DesecratedEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land before its controller chooses a card to discard")
    void destroysLandAndMakesControllerDiscard() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        KrakenHatchling discardedCard = new KrakenHatchling();
        harness.setHand(player1, List.of(new DesecratedEarth()));
        harness.setHand(player2, List.of(discardedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, land.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(land.getCard());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discardedCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KrakenHatchling());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DesecratedEarth()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys the land even when its controller has no cards to discard")
    void destroysLandWithEmptyHand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new DesecratedEarth()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, land.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(land.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An illegal land target prevents the entire spell from resolving")
    void doesNotDiscardWhenTargetLeavesBattlefield() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        KrakenHatchling cardInHand = new KrakenHatchling();
        harness.setHand(player1, List.of(new DesecratedEarth()));
        harness.setHand(player2, List.of(cardInHand));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, land.getId());
        gd.playerBattlefields.get(player2.getId()).remove(land);
        harness.setGraveyard(player2, List.of(land.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(cardInHand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeting your own land does not enable an opponent-caused discard replacement")
    @CardUsed({ObstinateBaloth.class})
    void ownSpellDiscardsBalothToGraveyard() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        ObstinateBaloth baloth = new ObstinateBaloth();
        harness.setHand(player1, List.of(new DesecratedEarth(), baloth));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, land.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard(), baloth);
        harness.assertNotOnBattlefield(player1, "Obstinate Baloth");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
