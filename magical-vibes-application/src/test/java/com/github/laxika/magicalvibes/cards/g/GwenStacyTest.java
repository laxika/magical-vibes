package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GwenStacy.class, GhostSpider.class, Forest.class, GrizzlyBears.class})
class GwenStacyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card on entering and keeps it playable after transforming")
    void exilesTopCardAndKeepsPermissionAfterTransforming() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GwenStacy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        prepareMainPhase();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent gwen = findPermanent(player1, "Gwen Stacy");
        assertThat(gd.getCardsExiledByPermanent(gwen.getId())).contains(topCard);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gwen.isTransformed()).isTrue();

        harness.clearPriorityPassed();
        gs.playCardFromExile(gd, player1, topCard.getId(), null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gwen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a counter on itself when its controller casts a spell from exile")
    void countersWhenControllerCastsSpellFromExile() {
        Permanent ghostSpider = addBackReady(player1);
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell, ghostSpider.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        prepareMainPhase();
        gs.playCardFromExile(gd, player1, spell.getId(), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ghostSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removes two counters to exile the top card for the turn")
    void removesCountersToExileTopCardForTheTurn() {
        Permanent ghostSpider = addBackReady(player1);
        ghostSpider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ghostSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    private Permanent addBackReady(Player player) {
        GwenStacy card = new GwenStacy();
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
