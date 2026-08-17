package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GastalThrillseekerTest extends BaseCardTest {

    @Test
    void entersDealsDamageToTargetOpponentAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GastalThrillseeker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void maxSpeedGrantsDeathtouchAndHaste() {
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addToBattlefield(player1, new GastalThrillseeker());

        Permanent thrillseeker = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();

        assertThat(gqs.hasKeyword(gd, thrillseeker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, thrillseeker, Keyword.HASTE)).isTrue();
    }

    @Test
    void belowMaxSpeedDoesNotGrantDeathtouchOrHaste() {
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.addToBattlefield(player1, new GastalThrillseeker());

        Permanent thrillseeker = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();

        assertThat(gqs.hasKeyword(gd, thrillseeker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, thrillseeker, Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotTargetControllerWithEnterTheBattlefieldAbility() {
        harness.setHand(player1, List.of(new GastalThrillseeker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
