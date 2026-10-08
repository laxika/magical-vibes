package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.s.SealOfRemoval;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChieftainEnDal.class, DefiantFalcon.class, SealOfRemoval.class, Humble.class})
class ChieftainEnDalTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Chieftain en-Dal grants first strike to all attacking creatures")
    void attackGrantsFirstStrikeToAttackingCreatures() {
        Permanent chieftain = addCreatureReady(player1, new ChieftainEnDal());
        Permanent attacker = addCreatureReady(player1, new DefiantFalcon());
        Permanent nonAttacker = addCreatureReady(player1, new DefiantFalcon());
        Permanent opponentCreature = addCreatureReady(player2, new DefiantFalcon());

        attackWithChieftainAndCreature();

        assertThat(gqs.hasKeyword(gd, chieftain, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike granted by Chieftain en-Dal wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ChieftainEnDal());
        Permanent attacker = addCreatureReady(player1, new DefiantFalcon());

        attackWithChieftainAndCreature();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant first strike when Chieftain en-Dal does not attack")
    void doesNotGrantFirstStrikeWhenChieftainDoesNotAttack() {
        Permanent chieftain = addCreatureReady(player1, new ChieftainEnDal());
        Permanent attacker = addCreatureReady(player1, new DefiantFalcon());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, chieftain, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Attack trigger still grants first strike after Chieftain en-Dal leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent chieftain = addCreatureReady(player1, new ChieftainEnDal());
        Permanent attacker = addCreatureReady(player1, new DefiantFalcon());
        harness.addToBattlefield(player2, new SealOfRemoval());

        declareAttackers(List.of(0, 1));
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();

        harness.activateAbility(player2, 0, null, chieftain.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Chieftain en-Dal");
        harness.assertInHand(player1, "Chieftain en-Dal");

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Attacker bounced and replayed after resolution does not retain first strike")
    void returnedCreatureDoesNotRetainFirstStrike() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new ChieftainEnDal());
        Permanent attacker = addCreatureReady(player1, new DefiantFalcon());
        harness.addToBattlefield(player2, new SealOfRemoval());

        attackWithChieftainAndCreature();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();

        harness.activateAbility(player2, 0, null, attacker.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Defiant Falcon");
        harness.assertInHand(player1, "Defiant Falcon");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DefiantFalcon)
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike granted after Humble resolves survives the earlier ability removal")
    void laterFirstStrikeGrantSurvivesEarlierAbilityRemoval() {
        addCreatureReady(player1, new ChieftainEnDal());
        Permanent attacker = addCreatureReady(player1, new DefiantFalcon());
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0, 1));
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();
    }

    private void attackWithChieftainAndCreature() {
        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
    }
}
