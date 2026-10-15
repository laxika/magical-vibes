package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ApostleOfInvasion.class})
class ApostleOfInvasionTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have double strike when opponent has fewer than three poison counters")
    void noDoubleStrikeBelowCorruptedThreshold() {
        Permanent apostle = harness.addToBattlefieldAndReturn(player1, new ApostleOfInvasion());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike when opponent has three poison counters")
    void hasDoubleStrikeAtCorruptedThreshold() {
        Permanent apostle = harness.addToBattlefieldAndReturn(player1, new ApostleOfInvasion());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not count the controller's poison counters")
    void doesNotCountControllersPoisonCounters() {
        Permanent apostle = harness.addToBattlefieldAndReturn(player1, new ApostleOfInvasion());
        gd.playerPoisonCounters.put(player1.getId(), 3);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses double strike when opponent's poison counters fall below three")
    void losesDoubleStrikeBelowCorruptedThreshold() {
        Permanent apostle = harness.addToBattlefieldAndReturn(player1, new ApostleOfInvasion());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerPoisonCounters.put(player2.getId(), 2);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gains double strike immediately when the opponent reaches three poison counters")
    void gainsDoubleStrikeWhenThresholdIsReached() {
        Permanent apostle = harness.addToBattlefieldAndReturn(player1, new ApostleOfInvasion());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isFalse();

        gd.playerPoisonCounters.put(player2.getId(), 4);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Corrupted uses the current controller's opponent after control changes")
    void reevaluatesOpponentAfterControlChanges() {
        Permanent apostle = harness.addToBattlefieldAndReturn(player1, new ApostleOfInvasion());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(apostle);
        gd.playerBattlefields.get(player2.getId()).add(apostle);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isFalse();

        gd.playerPoisonCounters.put(player1.getId(), 3);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Deals damage in both combat damage steps while corrupted")
    void dealsDoubleStrikeCombatDamage() {
        addCreatureReady(player1, new ApostleOfInvasion());
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 3);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Deals damage only once when the opponent is below the corrupted threshold")
    void dealsNormalCombatDamageBelowThreshold() {
        addCreatureReady(player1, new ApostleOfInvasion());
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 2);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }
}
