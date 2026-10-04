package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndlessRanksOfTheDead.class, WalkingCorpse.class, DarkthicketWolf.class})
class EndlessRanksOfTheDeadTest extends BaseCardTest {

    private List<Permanent> getZombieTokens(Player player) {
        return findPermanents(player, "Zombie").stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }

    @Test
    @DisplayName("Creates no tokens when no Zombies are controlled")
    void noTokensWithNoZombies() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getZombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Creates no tokens with 1 Zombie (half of 1 rounded down is 0)")
    void noTokensWithOneZombie() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new WalkingCorpse());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getZombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Creates 1 token with 2 Zombies (half of 2 is 1)")
    void oneTokenWithTwoZombies() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getZombieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Creates 1 token with 3 Zombies (half of 3 rounded down is 1)")
    void oneTokenWithThreeZombies() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getZombieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Creates 2 tokens with 4 Zombies (half of 4 is 2)")
    void twoTokensWithFourZombies() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getZombieTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Non-Zombie creatures do not count toward the total")
    void nonZombiesDoNotCount() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new WalkingCorpse()); // Zombie
        harness.addToBattlefield(player1, new DarkthicketWolf()); // Wolf, not Zombie

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Only 1 Zombie → half rounded down = 0 tokens
        assertThat(getZombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Opponent's Zombies do not count toward the total")
    void opponentsZombiesDoNotCount() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new WalkingCorpse());  // own Zombie
        harness.addToBattlefield(player2, new WalkingCorpse());  // opponent's Zombie

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Only 1 own Zombie → half rounded down = 0 tokens
        assertThat(getZombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());

        advanceToUpkeep(player2); // opponent's upkeep
        harness.passBothPriorities();

        assertThat(getZombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Zombie tokens created by previous triggers count on subsequent upkeeps")
    void tokensCountOnSubsequentUpkeeps() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());

        // First upkeep: 2 Zombies → 1 token
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger
        assertThat(getZombieTokens(player1)).hasSize(1);

        // Second upkeep: 3 Zombies (2 original + 1 token) → 1 more token
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger
        assertThat(getZombieTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Counts Zombies at resolution even if none were present when the ability triggered")
    void countsZombiesAddedAfterTriggering() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.passBothPriorities();

        assertThat(getZombieTokens(player1)).hasSize(1);
        assertThat(getZombieTokens(player2)).isEmpty();
        Permanent token = getZombieTokens(player1).getFirst();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Zombies that leave before resolution no longer count")
    void doesNotCountZombiesRemovedBeforeResolution() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new WalkingCorpse());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(zombie);
        gd.playerGraveyards.get(player1.getId()).add(zombie.getCard());
        harness.passBothPriorities();

        assertThat(getZombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Each copy counts tokens created by earlier resolving upkeep abilities")
    void multipleCopiesCountEarlierTokens() {
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(getZombieTokens(player1)).hasSize(1);
        harness.passBothPriorities();

        assertThat(getZombieTokens(player1)).hasSize(3);
    }

    @Test
    @DisplayName("The upkeep ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EndlessRanksOfTheDead());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(getZombieTokens(player1)).hasSize(1);
    }
}
