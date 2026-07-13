package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemorySluiceTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards from the top of the target player's library")
    void millsFourCards() {
        harness.setHand(player1, List.of(new MemorySluice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        Card topCard = deck.get(0);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new MemorySluice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        // 4 milled cards + Memory Sluice itself after resolving
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Conspire taps two color-sharing creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        harness.setHand(player1, List.of(new MemorySluice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent zombie1 = addCreatureReady(player1, new ScatheZombies());
        Permanent zombie2 = addCreatureReady(player1, new ZombieGoliath());

        harness.castWithConspire(player1, 0, player2.getId(), List.of(zombie1.getId(), zombie2.getId()));

        assertThat(zombie1.isTapped()).isTrue();
        assertThat(zombie2.isTapped()).isTrue();

        // The spell plus one conspire copy trigger are on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }
}
