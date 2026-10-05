package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianSwarmlord.class})
class PhyrexianSwarmlordTest extends BaseCardTest {

    @Test
    @DisplayName("No tokens created when opponent has no poison counters")
    void noTokensWhenNoPoisonCounters() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = findPermanents(player1, "Phyrexian Insect");

        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("Creates tokens equal to opponent's poison counters")
    void createsTokensEqualToOpponentPoisonCounters() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = findPermanents(player1, "Phyrexian Insect");

        assertThat(tokens).hasSize(3);

        Permanent insectToken = tokens.getFirst();
        assertThat(insectToken.getCard().getName()).isEqualTo("Phyrexian Insect");
        assertThat(insectToken.getCard().getPower()).isEqualTo(1);
        assertThat(insectToken.getCard().getToughness()).isEqualTo(1);
        assertThat(insectToken.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(insectToken.getCard().getSubtypes()).containsExactly(CardSubtype.PHYREXIAN, CardSubtype.INSECT);
        assertThat(insectToken.getCard().getKeywords()).contains(Keyword.INFECT);
        assertThat(insectToken.getCard().getType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    @DisplayName("Controller's own poison counters do not create tokens")
    void controllerPoisonCountersDoNotCount() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player1.getId(), 5);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = findPermanents(player1, "Phyrexian Insect");

        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        advanceToUpkeep(player2); // opponent's upkeep
        assertThat(gd.stack).isEmpty();

        List<Permanent> tokens = findPermanents(player1, "Phyrexian Insect");

        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("Creates tokens on each upkeep, accumulating over multiple turns")
    void tokensAccumulateOverMultipleUpkeeps() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        // First upkeep - 2 tokens
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Second upkeep - 2 more tokens
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Phyrexian Insect");

        assertThat(tokens).hasSize(4);
    }

    @Test
    @DisplayName("Token count increases as opponent gains more poison counters")
    void tokenCountScalesWithPoisonIncrease() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        // First upkeep - 1 token
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Phyrexian Insect");
        assertThat(tokens).hasSize(1);

        // Opponent gains more poison
        gd.playerPoisonCounters.put(player2.getId(), 4);

        // Second upkeep - 4 more tokens
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        tokens = findPermanents(player1, "Phyrexian Insect");
        assertThat(tokens).hasSize(5); // 1 + 4
    }

    @Test
    @DisplayName("Poison gained after the upkeep trigger is counted at resolution")
    void countsPoisonAtResolution() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Insect")).hasSize(3);
    }

    @Test
    @DisplayName("Poison removed before resolution no longer creates tokens")
    void removedPoisonIsNotCounted() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerPoisonCounters.put(player2.getId(), 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Insect")).isEmpty();
    }

    @Test
    @DisplayName("The upkeep trigger resolves after Swarmlord leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        Permanent swarmlord = findPermanent(player1, "Phyrexian Swarmlord");
        gd.playerBattlefields.get(player1.getId()).remove(swarmlord);
        gd.playerGraveyards.get(player1.getId()).add(swarmlord.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Insect")).hasSize(2);
    }

    @Test
    @DisplayName("Player two's Swarmlord counts player one's poison and creates tokens for player two")
    void countsPoisonRelativeToController() {
        harness.addToBattlefield(player2, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerPoisonCounters.put(player2.getId(), 5);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Phyrexian Insect")).hasSize(2);
        assertThat(findPermanents(player1, "Phyrexian Insect")).isEmpty();
    }

    @Test
    @DisplayName("Swarmlord deals combat damage to a player as poison rather than life loss")
    void swarmlordCombatDamageGivesPoison() {
        addCreatureReady(player1, new PhyrexianSwarmlord());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Created Insect tokens deal combat damage as poison rather than life loss")
    void tokenCombatDamageGivesPoison() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Phyrexian Insect");
        token.setSummoningSick(false);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(token)));
        resolveCombat();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }
}
