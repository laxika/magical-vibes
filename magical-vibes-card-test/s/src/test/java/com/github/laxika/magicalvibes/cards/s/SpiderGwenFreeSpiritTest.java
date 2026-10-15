package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderGwenFreeSpirit.class, Forest.class, GrizzlyBears.class})
class SpiderGwenFreeSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("When Spider-Gwen becomes tapped, discarding a card draws a card")
    void tappingSpiderGwenMayDiscardAndDraw() {
        Permanent gwen = addCreatureReady(player1, new SpiderGwenFreeSpirit());
        Card discard = new GrizzlyBears();
        Card draw = new Forest();
        harness.setHand(player1, List.of(discard));
        harness.setLibrary(player1, List.of(draw));

        tap(gwen);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining Spider-Gwen's tap trigger does not discard or draw")
    void decliningTapTriggerDoesNothing() {
        Permanent gwen = addCreatureReady(player1, new SpiderGwenFreeSpirit());
        Card draw = new Forest();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(draw));

        tap(gwen);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Spider-Gwen does not trigger when another permanent becomes tapped")
    void tappingAnotherPermanentDoesNotTrigger() {
        addCreatureReady(player1, new SpiderGwenFreeSpirit());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        tap(bears);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Spider-Gwen cannot draw without a card to discard")
    void emptyHandDoesNotDraw() {
        Permanent gwen = addCreatureReady(player1, new SpiderGwenFreeSpirit());
        Card draw = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));

        tap(gwen);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Spider-Gwen's tap trigger resolves after she leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent gwen = addCreatureReady(player1, new SpiderGwenFreeSpirit());
        Card discard = new Forest();
        Card draw = new Forest();
        harness.setHand(player1, List.of(discard));
        harness.setLibrary(player1, List.of(draw));

        tap(gwen);
        gd.playerBattlefields.get(player1.getId()).remove(gwen);
        gd.playerGraveyards.get(player1.getId()).add(gwen.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
    }

    @Test
    @DisplayName("Spider-Gwen triggers again when untapped and tapped in the same turn")
    void triggersForEachTap() {
        Permanent gwen = addCreatureReady(player1, new SpiderGwenFreeSpirit());
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        harness.setHand(player1, List.of(first));
        harness.setLibrary(player1, List.of(second, third));

        tap(gwen);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        gwen.untap();
        tap(gwen);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
