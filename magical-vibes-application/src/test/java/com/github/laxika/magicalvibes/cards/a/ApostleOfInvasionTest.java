package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApostleOfInvasionTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have double strike when opponent has fewer than three poison counters")
    void noDoubleStrikeBelowCorruptedThreshold() {
        Permanent apostle = addToBattlefield(player1, new ApostleOfInvasion());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike when opponent has three poison counters")
    void hasDoubleStrikeAtCorruptedThreshold() {
        Permanent apostle = addToBattlefield(player1, new ApostleOfInvasion());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not count the controller's poison counters")
    void doesNotCountControllersPoisonCounters() {
        Permanent apostle = addToBattlefield(player1, new ApostleOfInvasion());
        gd.playerPoisonCounters.put(player1.getId(), 3);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses double strike when opponent's poison counters fall below three")
    void losesDoubleStrikeBelowCorruptedThreshold() {
        Permanent apostle = addToBattlefield(player1, new ApostleOfInvasion());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerPoisonCounters.put(player2.getId(), 2);

        assertThat(gqs.hasKeyword(gd, apostle, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addToBattlefield(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
