package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilkweaverElite.class, DruidOfTheCowl.class})
class SilkweaverEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when your permanent left the battlefield this turn")
    void drawsCardAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        Card drawnCard = new DruidOfTheCowl();
        harness.setLibrary(player1, List.of(drawnCard));

        castSilkweaverElite();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Does not draw a card when no permanent left the battlefield")
    void doesNotDrawCardWithoutRevolt() {
        Card drawnCard = new DruidOfTheCowl();
        harness.setLibrary(player1, List.of(drawnCard));

        castSilkweaverElite();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Does not draw a card when only an opponent's permanent left the battlefield")
    void doesNotDrawCardAfterOpponentsPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        Card drawnCard = new DruidOfTheCowl();
        harness.setLibrary(player1, List.of(drawnCard));

        castSilkweaverElite();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Revolt counts a permanent that leaves after casting but before entry")
    void drawsWhenPermanentLeavesWhileSpellIsOnStack() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        Card drawnCard = new DruidOfTheCowl();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new SilkweaverElite()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, permanent));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("A departure after entry cannot retroactively trigger revolt")
    void doesNotTriggerWhenPermanentLeavesOnlyAfterEntry() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        Card drawnCard = new DruidOfTheCowl();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new SilkweaverElite()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, permanent));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("The revolt trigger still draws after Silkweaver Elite leaves")
    void drawsAfterSourceLeavesBeforeTriggerResolves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        Card drawnCard = new DruidOfTheCowl();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new SilkweaverElite()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        Permanent elite = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, elite));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private void castSilkweaverElite() {
        harness.setHand(player1, List.of(new SilkweaverElite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
