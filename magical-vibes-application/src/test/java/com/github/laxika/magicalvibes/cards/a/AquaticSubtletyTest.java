package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZephyrSprite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AquaticSubtlety.class, GrizzlyBears.class, ZephyrSprite.class})
class AquaticSubtletyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two, bottoms two, and perpetually grants Evoke to blue creature cards left in hand")
    void drawsBottomsAndGrantsEvoke() {
        GrizzlyBears firstBottom = new GrizzlyBears();
        GrizzlyBears secondBottom = new GrizzlyBears();
        GrizzlyBears remainingNonblue = new GrizzlyBears();
        ZephyrSprite firstBlue = new ZephyrSprite();
        ZephyrSprite secondBlue = new ZephyrSprite();
        harness.setHand(player1, List.of(new AquaticSubtlety(), firstBottom, secondBottom, remainingNonblue));
        harness.setLibrary(player1, List.of(firstBlue, secondBlue));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstBottom, secondBottom, remainingNonblue, firstBlue, secondBlue);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(firstBottom.getId(), secondBottom.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remainingNonblue, firstBlue, secondBlue);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstBottom, secondBottom);

        assertThatThrownBy(() -> harness.getGameService().playCardWithAlternateCost(
                gd, player1, 0, 0, null, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A blue creature can be cast for granted Evoke by exiling another blue card")
    void castsBlueCreatureWithGrantedEvoke() {
        GrizzlyBears firstBottom = new GrizzlyBears();
        GrizzlyBears secondBottom = new GrizzlyBears();
        GrizzlyBears remainingNonblue = new GrizzlyBears();
        ZephyrSprite evoked = new ZephyrSprite();
        ZephyrSprite payment = new ZephyrSprite();
        harness.setHand(player1, List.of(new AquaticSubtlety(), firstBottom, secondBottom, remainingNonblue));
        harness.setLibrary(player1, List.of(evoked, payment));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(firstBottom.getId(), secondBottom.getId()));

        SpellCastingService spellCastingService = GameTestEngineContext.get().getBean(SpellCastingService.class);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.inMutationScope(() -> spellCastingService.playCardWithAlternateCost(
                gd, player1, 1, 0, null, null, List.of(), 2));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Zephyr Sprite");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(payment);
    }
}
