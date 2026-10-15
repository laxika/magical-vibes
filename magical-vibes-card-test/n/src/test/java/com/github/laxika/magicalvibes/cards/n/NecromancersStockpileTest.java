package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecromancersStockpile.class, BlackCat.class, RuneclawBear.class, Ornithopter.class,
        Negate.class, Naturalize.class})
class NecromancersStockpileTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a nonZombie creature card only draws a card")
    void discardingNonZombieDrawsOnly() {
        setUpStockpile();
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInHand(player1, "Ornithopter");
        assertThat(zombieTokens()).isEmpty();
    }

    @Test
    @DisplayName("Discarding a Zombie card draws a card and creates a tapped 2/2 Zombie token")
    void discardingZombieCreatesTappedToken() {
        setUpStockpile();
        harness.setHand(player1, List.of(new BlackCat()));
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Black Cat");
        harness.assertInHand(player1, "Ornithopter");
        assertThat(zombieTokens()).hasSize(1);
        Permanent token = zombieTokens().getFirst();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("A later nonZombie discard does not repeat the earlier Zombie token")
    void secondActivationRechecksDiscardedCard() {
        setUpStockpile();
        harness.setHand(player1, List.of(new BlackCat(), new RuneclawBear()));
        harness.setLibrary(player1, List.of(new Ornithopter(), new Ornithopter()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(zombieTokens()).hasSize(1);

        harness.activateAbility(player1, 0, 0, null);
        List<com.github.laxika.magicalvibes.model.Card> hand = gd.playerHands.get(player1.getId());
        int bearsIndex = hand.indexOf(hand.stream()
                .filter(c -> c.getName().equals("Runeclaw Bear")).findFirst().orElseThrow());
        harness.handleCardChosen(player1, bearsIndex);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(zombieTokens()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without a creature card to discard")
    void cannotActivateWithoutCreatureCard() {
        setUpStockpile();
        harness.setHand(player1, List.of(new Negate()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each stacked activation remembers its own discarded card")
    void stackedActivationsRememberTheirOwnDiscards() {
        setUpStockpile();
        harness.setHand(player1, List.of(new BlackCat(), new RuneclawBear()));
        harness.setLibrary(player1, List.of(new Ornithopter(), new Ornithopter()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Black Cat");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(zombieTokens()).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(zombieTokens()).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(zombieTokens()).hasSize(1);
        assertThat(zombieTokens().getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability resolves after Stockpile is destroyed")
    void resolvesAfterSourceLeavesBattlefield() {
        setUpStockpile();
        harness.setHand(player1, List.of(new BlackCat()));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Necromancer's Stockpile"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Necromancer's Stockpile");
        harness.assertNotInHand(player1, "Ornithopter");

        harness.passBothPriorities();
        harness.assertInHand(player1, "Ornithopter");
        assertThat(zombieTokens()).hasSize(1);
        assertThat(zombieTokens().getFirst().isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private List<Permanent> zombieTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
    }

    private void setUpStockpile() {
        harness.addToBattlefield(player1, new NecromancersStockpile());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
