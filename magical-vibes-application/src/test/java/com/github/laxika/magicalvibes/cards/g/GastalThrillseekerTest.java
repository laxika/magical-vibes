package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GastalThrillseeker.class})
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
        Permanent thrillseeker = harness.addToBattlefieldAndReturn(player1, new GastalThrillseeker());

        assertThat(gqs.hasKeyword(gd, thrillseeker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, thrillseeker, Keyword.HASTE)).isTrue();
    }

    @Test
    void belowMaxSpeedDoesNotGrantDeathtouchOrHaste() {
        gd.playerSpeeds.put(player1.getId(), 3);
        Permanent thrillseeker = harness.addToBattlefieldAndReturn(player1, new GastalThrillseeker());

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

    @Test
    void enteringStartsEnginesBeforeItsDamageIncreasesSpeed() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GastalThrillseeker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void enteringAtSpeedThreeReachesMaxSpeedAndGainsKeywords() {
        harness.forceActivePlayer(player1);
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new GastalThrillseeker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        Permanent thrillseeker = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, thrillseeker, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, thrillseeker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, thrillseeker, Keyword.HASTE)).isTrue();
    }

    @Test
    void opponentsMaxSpeedDoesNotGrantKeywords() {
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);
        Permanent thrillseeker = harness.addToBattlefieldAndReturn(player1, new GastalThrillseeker());

        assertThat(gqs.hasKeyword(gd, thrillseeker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, thrillseeker, Keyword.HASTE)).isFalse();
    }
}
