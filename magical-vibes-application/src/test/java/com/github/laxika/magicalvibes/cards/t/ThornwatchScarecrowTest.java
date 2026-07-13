package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThornwatchScarecrowTest extends BaseCardTest {

    // ===== Wither while you control a green creature =====

    @Test
    @DisplayName("Has wither while you control a green creature")
    void witherWithGreenCreature() {
        harness.addToBattlefield(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new GrizzlyBears()); // green

        Permanent scarecrow = findPermanent(player1, "Thornwatch Scarecrow");
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("No wither without a green creature, and none from an opponent's green creature")
    void noWitherWithoutOwnGreenCreature() {
        harness.addToBattlefield(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player2, new GrizzlyBears()); // opponent's green

        Permanent scarecrow = findPermanent(player1, "Thornwatch Scarecrow");
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Loses wither when the green creature leaves the battlefield")
    void losesWitherWhenGreenLeaves() {
        harness.addToBattlefield(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent scarecrow = findPermanent(player1, "Thornwatch Scarecrow");
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard() instanceof GrizzlyBears);

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();
    }

    // ===== Vigilance while you control a white creature =====

    @Test
    @DisplayName("Has vigilance while you control a white creature")
    void vigilanceWithWhiteCreature() {
        harness.addToBattlefield(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new EliteVanguard()); // white

        Permanent scarecrow = findPermanent(player1, "Thornwatch Scarecrow");
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("No vigilance without a white creature, and none from an opponent's white creature")
    void noVigilanceWithoutOwnWhiteCreature() {
        harness.addToBattlefield(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player2, new EliteVanguard()); // opponent's white

        Permanent scarecrow = findPermanent(player1, "Thornwatch Scarecrow");
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
    }

    // ===== The two grants are independent =====

    @Test
    @DisplayName("A green creature grants wither but not vigilance")
    void greenGrantsWitherNotVigilance() {
        harness.addToBattlefield(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new GrizzlyBears()); // green only

        Permanent scarecrow = findPermanent(player1, "Thornwatch Scarecrow");
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
    }
}
