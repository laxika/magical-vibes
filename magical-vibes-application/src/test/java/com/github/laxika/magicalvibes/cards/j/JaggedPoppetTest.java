package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CacklingFlames;
import com.github.laxika.magicalvibes.cards.g.GnatAlleyCreeper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JaggedPoppet.class, CacklingFlames.class, GnatAlleyCreeper.class})
class JaggedPoppetTest extends BaseCardTest {

    @Test
    @DisplayName("When dealt damage, its controller discards cards equal to the damage")
    void controllerDiscardsCardsEqualToDamageReceived() {
        Permanent poppet = addCreatureReady(player1, new JaggedPoppet());
        harness.setHand(player1, List.of(
                new GnatAlleyCreeper(), new GnatAlleyCreeper(), new GnatAlleyCreeper()));
        harness.setHand(player2, List.of(new CacklingFlames(), new GnatAlleyCreeper()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0, poppet.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Hellbent makes combat damage cause the damaged player to discard that much")
    void hellbentCombatDamageMakesDamagedPlayerDiscardEqualToDamage() {
        Permanent poppet = addCreatureReady(player1, new JaggedPoppet());
        poppet.setAttacking(true);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(
                new GnatAlleyCreeper(), new GnatAlleyCreeper(), new GnatAlleyCreeper()));

        resolveCombat();
        harness.passBothPriorities();

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Hellbent does not trigger while its controller has cards in hand")
    void hellbentDoesNotTriggerWithCardsInHand() {
        Permanent poppet = addCreatureReady(player1, new JaggedPoppet());
        poppet.setAttacking(true);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new GnatAlleyCreeper()));
        harness.setHand(player2, List.of(
                new GnatAlleyCreeper(), new GnatAlleyCreeper(), new GnatAlleyCreeper()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Hellbent is checked when combat damage is dealt")
    void hellbentDoesNotTriggerIfHandWasNotEmptyWhenDamageWasDealt() {
        Permanent poppet = addCreatureReady(player1, new JaggedPoppet());
        poppet.setAttacking(true);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new GnatAlleyCreeper()));
        harness.setHand(player2, List.of(
                new GnatAlleyCreeper(), new GnatAlleyCreeper(), new GnatAlleyCreeper()));

        resolveCombat();
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
