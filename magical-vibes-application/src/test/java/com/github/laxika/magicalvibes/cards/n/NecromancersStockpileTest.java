package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NecromancersStockpileTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a nonZombie creature card only draws a card")
    void discardingNonZombieDrawsOnly() {
        setUpStockpile();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Ornithopter");
        assertThat(zombieTokens()).isEmpty();
    }

    @Test
    @DisplayName("Discarding a Zombie card draws a card and creates a tapped 2/2 Zombie token")
    void discardingZombieCreatesTappedToken() {
        setUpStockpile();
        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertInHand(player1, "Ornithopter");
        assertThat(zombieTokens()).hasSize(1);
        Permanent token = zombieTokens().getFirst();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A later nonZombie discard does not repeat the earlier Zombie token")
    void secondActivationRechecksDiscardedCard() {
        setUpStockpile();
        harness.setHand(player1, List.of(new WalkingCorpse(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Ornithopter(), new Ornithopter()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(zombieTokens()).hasSize(1);

        harness.activateAbility(player1, 0, 0, null);
        List<com.github.laxika.magicalvibes.model.Card> hand = gd.playerHands.get(player1.getId());
        int bearsIndex = hand.indexOf(hand.stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).findFirst().orElseThrow());
        harness.handleCardChosen(player1, bearsIndex);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(zombieTokens()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without a creature card to discard")
    void cannotActivateWithoutCreatureCard() {
        setUpStockpile();
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.d.Disperse()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
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
