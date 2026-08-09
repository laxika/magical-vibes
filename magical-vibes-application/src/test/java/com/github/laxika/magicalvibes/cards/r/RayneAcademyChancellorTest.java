package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RayneAcademyChancellorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws when an opponent's spell targets you")
    void drawsWhenOpponentTargetsYou() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws when an opponent's spell targets a noncreature permanent you control")
    void drawsWhenOpponentTargetsNoncreaturePermanent() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of());
        UUID islandId = harness.getPermanentId(player1, "Island");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, islandId);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws an additional card when enchanted")
    void drawsAdditionalCardWhenEnchanted() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        UUID rayneId = harness.getPermanentId(player1, "Rayne, Academy Chancellor");

        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, rayneId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Does not trigger for your own spell")
    void doesNotTriggerForOwnSpell() {
        harness.addToBattlefield(player1, new RayneAcademyChancellor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }
}
