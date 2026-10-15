package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.r.Reconnaissance;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NamorAtlanteanKing.class, Shock.class, GrizzlyBears.class, JaceBeleren.class, Reconnaissance.class})
class NamorAtlanteanKingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell creates a blue Merfolk token")
    void noncreatureSpellCreatesMerfolkToken() {
        harness.addToBattlefield(player1, new NamorAtlanteanKing());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Merfolk");
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Merfolk token")
    void creatureSpellDoesNotCreateMerfolkToken() {
        harness.addToBattlefield(player1, new NamorAtlanteanKing());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Merfolk")).isZero();
    }

    @Test
    @DisplayName("Attacking a higher-life player boosts other creatures attacking that player")
    void attacksHigherLifePlayerBoostsOtherAttackers() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent namor = addCreatureReady(player1, new NamorAtlanteanKing());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, namor)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost when the attacked player does not have more life")
    void doesNotBoostWhenAttackedPlayerDoesNotHaveMoreLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent namor = addCreatureReady(player1, new NamorAtlanteanKing());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, namor)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(2);
    }

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("Does not boost a creature attacking the player's planeswalker")
    void doesNotBoostCreatureAttackingPlaneswalker() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent namor = addCreatureReady(player1, new NamorAtlanteanKing());
        Permanent planeswalkerAttacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new JaceBeleren());
        Permanent planeswalker = findPermanent(player2, "Jace Beleren");

        declareAttackers(player1, List.of(0, 1), Map.of(
                0, player2.getId(),
                1, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, namor)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, planeswalkerAttacker)).isEqualTo(2);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices,
                                  Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    @Test
    void lifeChangesAfterAttackDoNotPreventBoost() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new NamorAtlanteanKing());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.setLife(player1, 30);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    void attackTriggerStillBoostsAfterNamorDies() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent namor = addCreatureReady(player1, new NamorAtlanteanKing());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(0, 1));
        harness.castInstant(player2, 0, namor.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Namor, Atlantean King");
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
    }

    @Test
    @CardUsed(JaceBeleren.class)
    void attackingPlaneswalkerDoesNotTriggerBoostForPlayerAttackers() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new NamorAtlanteanKing());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new JaceBeleren());
        Permanent planeswalker = findPermanent(player2, "Jace Beleren");

        declareAttackers(player1, List.of(0, 1), Map.of(
                0, planeswalker.getId(),
                1, player2.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @CardUsed(Reconnaissance.class)
    void removingNamorFromCombatDoesNotPreventOtherAttackersBoost() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent namor = addCreatureReady(player1, new NamorAtlanteanKing());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Reconnaissance());

        declareAttackers(List.of(0, 1));
        harness.activateAbility(player1, 2, null, namor.getId());
        resolveAllTriggers();

        assertThat(namor.isAttacking()).isFalse();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
    }

    @Test
    void opponentNoncreatureSpellDoesNotCreateToken() {
        harness.addToBattlefield(player1, new NamorAtlanteanKing());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Merfolk")).isZero();
    }

    @Test
    void tokenTriggerResolvesBeforeSpellAndSurvivesNamorsDeath() {
        harness.addToBattlefield(player1, new NamorAtlanteanKing());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, findPermanent(player1, "Namor, Atlantean King").getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Merfolk")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Namor, Atlantean King");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Namor, Atlantean King");
        assertThat(countPermanents(player1, "Merfolk")).isEqualTo(1);
    }
}
