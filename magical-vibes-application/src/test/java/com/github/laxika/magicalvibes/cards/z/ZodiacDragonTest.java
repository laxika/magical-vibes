package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.o.OverwhelmingForces;
import com.github.laxika.magicalvibes.cards.v.VolitionReins;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZodiacDragon.class, OverwhelmingForces.class, VolitionReins.class})
class ZodiacDragonTest extends BaseCardTest {

    @Test
    @DisplayName("When Zodiac Dragon dies, you may return it to your hand")
    void diesMayReturnToHand() {
        harness.addToBattlefield(player2, new ZodiacDragon());
        Permanent dragon = gd.playerBattlefields.get(player2.getId()).getFirst();
        Card dragonCard = dragon.getCard();

        // Overwhelming Forces resolves — the Dragon dies and its ON_DEATH may-trigger goes on the stack.
        harness.setHand(player1, List.of(new OverwhelmingForces()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve the death trigger — prompts the may-choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(dragonCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(dragonCard.getId()));
    }

    @Test
    @DisplayName("Declining the may-trigger leaves Zodiac Dragon in the graveyard")
    void decliningLeavesInGraveyard() {
        harness.addToBattlefield(player2, new ZodiacDragon());
        Permanent dragon = gd.playerBattlefields.get(player2.getId()).getFirst();
        Card dragonCard = dragon.getCard();

        harness.setHand(player1, List.of(new OverwhelmingForces()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(dragonCard.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(dragonCard.getId()));
    }

    @Test
    @DisplayName("A stolen Zodiac Dragon does not trigger from its owner's graveyard")
    void stolenDragonDoesNotTrigger() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new ZodiacDragon());
        Card dragonCard = dragon.getCard();

        harness.setHand(player1, List.of(new VolitionReins()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0, dragon.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(dragon.getId()));

        harness.setHand(player2, List.of(new OverwhelmingForces()));
        harness.addMana(player2, ManaColor.BLACK, 8);
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(dragonCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(dragonCard.getId()));
    }
}
