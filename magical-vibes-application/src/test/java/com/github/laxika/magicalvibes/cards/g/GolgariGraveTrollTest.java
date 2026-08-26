package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({GolgariGraveTroll.class, GrizzlyBears.class, GiantSpider.class, Shock.class, Forest.class})
class GolgariGraveTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each creature card in its controller's graveyard")
    void entersWithCountersPerCreatureCard() {
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GiantSpider());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        castTroll();

        Permanent troll = findPermanent(player1, "Golgari Grave-Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(troll.getEffectivePower()).isEqualTo(2);
        assertThat(troll.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter creates a regeneration shield")
    void removesCounterAndRegenerates() {
        Permanent troll = addReadyTroll(player1, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        addReadyTroll(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareTurn();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May dredge six cards instead of drawing")
    void dredgesInsteadOfDrawing() {
        GolgariGraveTroll troll = new GolgariGraveTroll();
        List<Card> milled = List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
        harness.setGraveyard(player1, List.of(troll));
        harness.setLibrary(player1, milled);

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(troll);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    private void castTroll() {
        harness.setHand(player1, List.of(new GolgariGraveTroll()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addReadyTroll(Player player, int counters) {
        Permanent troll = new Permanent(new GolgariGraveTroll());
        troll.setSummoningSick(false);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        gd.playerBattlefields.get(player.getId()).add(troll);
        return troll;
    }

    private void prepareTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
