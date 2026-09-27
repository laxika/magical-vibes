package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AssassinInitiate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MariTheKillingQuill.class, AssassinInitiate.class, GrizzlyBears.class, Murder.class})
class MariTheKillingQuillTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's creature dies and is exiled with a hit counter")
    void exilesOpponentCreatureWithHitCounter() {
        addCreatureReady(player1, new MariTheKillingQuill());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(victim.getCard());
        assertThat(gd.exiledCardHitCounters).containsEntry(victim.getCard().getId(), 1);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An outlaw's combat damage may remove a hit counter, draw, and create Treasures")
    void combatDamageUsesHitCounter() {
        addCreatureReady(player1, new MariTheKillingQuill());
        Permanent attacker = addCreatureReady(player1, new AssassinInitiate());
        attacker.setAttacking(true);
        Card exiled = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiled);
        gd.exiledCardHitCounters.put(exiled.getId(), 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HitCounterExiledCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HitCounterExiledCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(exiled.getId());
        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));

        assertThat(gd.exiledCardHitCounters).doesNotContainKey(exiled.getId());
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }
}
