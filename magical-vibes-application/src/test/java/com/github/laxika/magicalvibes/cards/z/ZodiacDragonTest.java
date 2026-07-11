package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZodiacDragonTest extends BaseCardTest {

    @Test
    @DisplayName("When Zodiac Dragon dies, you may return it to your hand")
    void diesMayReturnToHand() {
        harness.addToBattlefield(player1, new ZodiacDragon());
        Permanent dragon = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card dragonCard = dragon.getCard();

        // Wrath the board — the Dragon dies and its ON_DEATH may-trigger goes on the stack.
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Dragon dies, death trigger placed
        harness.passBothPriorities(); // resolve the death trigger — prompts the may-choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(dragonCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(dragonCard.getId()));
    }

    @Test
    @DisplayName("Declining the may-trigger leaves Zodiac Dragon in the graveyard")
    void decliningLeavesInGraveyard() {
        harness.addToBattlefield(player1, new ZodiacDragon());
        Permanent dragon = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card dragonCard = dragon.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(dragonCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(dragonCard.getId()));
    }
}
