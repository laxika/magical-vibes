package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveDefiler.class, GrizzlyBears.class, Plains.class, Shock.class, ZombieGoliath.class})
class GraveDefilerTest extends BaseCardTest {

    private void finishAnyReorder() {
        PendingInteraction.LibraryReorder reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.CardOrder(IntStream.range(0, reorder.cards().size()).boxed().toList()));
        }
    }

    private void castGraveDefiler() {
        harness.setHand(player1, List.of(new GraveDefiler()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Zombie cards among the top four go to hand and the rest go to the bottom")
    void zombieCardsGoToHand() {
        Card zombie1 = new ZombieGoliath();
        Card zombie2 = new ZombieGoliath();
        Card bear = new GrizzlyBears();
        Card plains = new Plains();
        Card shock = new Shock();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(zombie1, bear, zombie2, plains, shock));

        castGraveDefiler();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(zombie1, zombie2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bear, plains);
        assertThat(deck).contains(bear, plains);
    }

    @Test
    @DisplayName("Only the top four cards are revealed")
    void onlyTopFourAreRevealed() {
        Card bear1 = new GrizzlyBears();
        Card plains = new Plains();
        Card shock = new Shock();
        Card bear2 = new GrizzlyBears();
        Card deepZombie = new ZombieGoliath();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(bear1, plains, shock, bear2, deepZombie));

        castGraveDefiler();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(deepZombie);
        assertThat(deck).contains(deepZombie);
    }

    @Test
    @DisplayName("Paying {1}{B} grants Grave Defiler a regeneration shield")
    void payGrantsRegenerationShield() {
        Permanent defiler = addCreatureReady(player1, new GraveDefiler());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(defiler.getRegenerationShield()).isEqualTo(1);
    }
}
