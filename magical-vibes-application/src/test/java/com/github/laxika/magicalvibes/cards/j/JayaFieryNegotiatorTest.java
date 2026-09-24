package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JayaFieryNegotiator.class, GrizzlyBears.class, LightningBolt.class})
class JayaFieryNegotiatorTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a red Monk token with prowess")
    void plusOneCreatesMonkWithProwess() {
        addReadyJaya(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent monk = findPermanents(player1, "Monk").getFirst();
        assertThat(gqs.hasKeyword(gd, monk, Keyword.PROWESS)).isTrue();
    }

    @Test
    @DisplayName("-1 exiles two cards and grants play permission to the chosen card")
    void minusOneExilesAndPermitsChosenCard() {
        addReadyJaya(player1);
        Card first = new LightningBolt();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId());
    }

    @Test
    @DisplayName("-2 deals damage equal to the number of attacking creatures")
    void minusTwoDealsAttackerCountDamage() {
        addReadyJaya(player1);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("-8 creates two copy triggers for a red instant or sorcery")
    void minusEightCopiesRedInstantOrSorceryTwice() {
        Permanent jaya = addReadyJaya(player1);
        jaya.setCounterCount(CounterType.LOYALTY, 8);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && "Jaya, Fiery Negotiator's emblem".equals(e.getDescription()))
                .hasSize(2);
    }

    private Permanent addReadyJaya(Player player) {
        Permanent permanent = new Permanent(new JayaFieryNegotiator());
        permanent.setCounterCount(CounterType.LOYALTY, 5);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    protected Permanent addCreatureReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
