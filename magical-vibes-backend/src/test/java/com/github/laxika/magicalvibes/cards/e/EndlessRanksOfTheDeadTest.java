package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FesteringGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerControlledCreatureSubtypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EndlessRanksOfTheDeadTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private List<Permanent> getZombieTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie") && p.isToken())
                .toList();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct effect configuration")
    void hasCorrectProperties() {
        EndlessRanksOfTheDead card = new EndlessRanksOfTheDead();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(CreateTokensPerControlledCreatureSubtypeEffect.class);
        CreateTokensPerControlledCreatureSubtypeEffect effect =
                (CreateTokensPerControlledCreatureSubtypeEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(effect.subtype()).isEqualTo(CardSubtype.ZOMBIE);
        assertThat(effect.divisor()).isEqualTo(2);
        assertThat(effect.tokenName()).isEqualTo("Zombie");
        assertThat(effect.power()).isEqualTo(2);
        assertThat(effect.toughness()).isEqualTo(2);
        assertThat(effect.color()).isEqualTo(CardColor.BLACK);
    }

    // ===== Zero Zombies =====

    @Test
    @DisplayName("Creates no tokens when no Zombies are controlled")
    void noTokensWithNoZombies() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getZombieTokens(player1)).isEmpty();
    }

    // ===== One Zombie (rounds down to 0) =====

    @Test
    @DisplayName("Creates no tokens with 1 Zombie (half of 1 rounded down is 0)")
    void noTokensWithOneZombie() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new FesteringGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getZombieTokens(player1)).isEmpty();
    }

    // ===== Two Zombies (half of 2 = 1 token) =====

    @Test
    @DisplayName("Creates 1 token with 2 Zombies (half of 2 is 1)")
    void oneTokenWithTwoZombies() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new FesteringGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getZombieTokens(player1)).hasSize(1);
    }

    // ===== Three Zombies (half of 3 rounded down = 1 token) =====

    @Test
    @DisplayName("Creates 1 token with 3 Zombies (half of 3 rounded down is 1)")
    void oneTokenWithThreeZombies() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new FesteringGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getZombieTokens(player1)).hasSize(1);
    }

    // ===== Four Zombies (half of 4 = 2 tokens) =====

    @Test
    @DisplayName("Creates 2 tokens with 4 Zombies (half of 4 is 2)")
    void twoTokensWithFourZombies() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new FesteringGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getZombieTokens(player1)).hasSize(2);
    }

    // ===== Non-Zombie creatures do not count =====

    @Test
    @DisplayName("Non-Zombie creatures do not count toward the total")
    void nonZombiesDoNotCount() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new FesteringGoblin()); // Zombie
        harness.addToBattlefield(player1, new GrizzlyBears());    // Bear, not Zombie

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Only 1 Zombie → half rounded down = 0 tokens
        assertThat(getZombieTokens(player1)).isEmpty();
    }

    // ===== Opponent's Zombies do not count =====

    @Test
    @DisplayName("Opponent's Zombies do not count toward the total")
    void opponentsZombiesDoNotCount() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new FesteringGoblin());  // own Zombie
        harness.addToBattlefield(player2, new FesteringGoblin());  // opponent's Zombie

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Only 1 own Zombie → half rounded down = 0 tokens
        assertThat(getZombieTokens(player1)).isEmpty();
    }

    // ===== Does not trigger on opponent's upkeep =====

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new FesteringGoblin());

        advanceToUpkeep(player2); // opponent's upkeep
        harness.passBothPriorities();

        assertThat(getZombieTokens(player1)).isEmpty();
    }

    // ===== Zombie tokens from previous triggers count on subsequent upkeeps =====

    @Test
    @DisplayName("Zombie tokens created by previous triggers count on subsequent upkeeps")
    void tokensCountOnSubsequentUpkeeps() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new FesteringGoblin());

        // First upkeep: 2 Zombies → 1 token
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger
        assertThat(getZombieTokens(player1)).hasSize(1);

        // Second upkeep: 3 Zombies (2 original + 1 token) → 1 more token
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger
        assertThat(getZombieTokens(player1)).hasSize(2);
    }
}
