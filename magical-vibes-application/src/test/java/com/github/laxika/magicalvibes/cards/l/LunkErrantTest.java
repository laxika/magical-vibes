package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LunkErrant.class, ElvishWarrior.class})
class LunkErrantTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures on either battlefield do not prevent attacking alone")
    void attackingAloneWithOtherCreaturesOnBattlefield() {
        Permanent lunk = addCreatureReady(player1, new LunkErrant());
        Permanent other = addCreatureReady(player1, new LunkErrant());
        Permanent defender = addCreatureReady(player2, new ElvishWarrior());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, lunk)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, lunk, Keyword.TRAMPLE)).isFalse();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lunk)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, lunk)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, lunk, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, defender, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Two Lunk Errants attacking together do not trigger either ability")
    void twoLunkErrantsAttackingTogetherDoNotTrigger() {
        Permanent first = addCreatureReady(player1, new LunkErrant());
        Permanent second = addCreatureReady(player1, new LunkErrant());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1)));

        assertThat(gd.stack).isEmpty();
        for (Permanent lunk : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, lunk)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, lunk)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, lunk, Keyword.TRAMPLE)).isFalse();
        }
    }

    @Test
    @DisplayName("Attacking alone allows excess combat damage to trample over a blocker")
    void attackingAloneTramplesOverBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LunkErrant());
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Elvish Warrior");
        harness.assertOnBattlefield(player1, "Lunk Errant");
    }

    @Test
    @DisplayName("Attacking alone — gets +1/+1 and gains trample until end of turn")
    void attackingAloneBoostsAndGrantsTrample() {
        Permanent lunk = addCreatureReady(player1, new LunkErrant());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        assertThat(gqs.getEffectivePower(gd, lunk)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, lunk)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, lunk, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Attacking with another creature — no boost, no trample")
    void attackingWithOtherCreatureNoEffect() {
        Permanent lunk = addCreatureReady(player1, new LunkErrant());
        addCreatureReady(player1, new ElvishWarrior());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, lunk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lunk)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, lunk, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Attacking alone â€” boost and trample expire at end of turn")
    void attackingAloneEffectsExpireAtEndOfTurn() {
        Permanent lunk = addCreatureReady(player1, new LunkErrant());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, lunk)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, lunk, Keyword.TRAMPLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, lunk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lunk)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, lunk, Keyword.TRAMPLE)).isFalse();
    }
}
