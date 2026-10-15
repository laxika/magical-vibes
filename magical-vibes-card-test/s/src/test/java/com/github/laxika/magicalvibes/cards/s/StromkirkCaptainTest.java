package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.f.FalkenrathTorturer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StromkirkCaptain.class, FalkenrathTorturer.class, DawntreaderElk.class})
class StromkirkCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Other Vampire creatures you control get +1/+1 and first strike")
    void buffsOtherVampiresYouControl() {
        harness.addToBattlefield(player1, new StromkirkCaptain());
        Permanent torturer = harness.addToBattlefieldAndReturn(player1, new FalkenrathTorturer());

        assertThat(gqs.getEffectivePower(gd, torturer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, torturer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, torturer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Stromkirk Captain does not buff itself")
    void doesNotBuffItself() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new StromkirkCaptain());

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not buff non-Vampire creatures")
    void doesNotBuffNonVampires() {
        harness.addToBattlefield(player1, new StromkirkCaptain());
        Permanent elk = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());

        assertThat(gqs.getEffectivePower(gd, elk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elk)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elk, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Vampire creatures")
    void doesNotBuffOpponentVampires() {
        harness.addToBattlefield(player1, new StromkirkCaptain());
        Permanent opponentTorturer = harness.addToBattlefieldAndReturn(player2, new FalkenrathTorturer());

        assertThat(gqs.getEffectivePower(gd, opponentTorturer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentTorturer)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentTorturer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Bonus is removed when Stromkirk Captain leaves the battlefield")
    void bonusRemovedWhenCaptainLeaves() {
        harness.addToBattlefield(player1, new StromkirkCaptain());
        Permanent torturer = harness.addToBattlefieldAndReturn(player1, new FalkenrathTorturer());

        assertThat(gqs.getEffectivePower(gd, torturer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, torturer, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Stromkirk Captain"));

        assertThat(gqs.getEffectivePower(gd, torturer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, torturer)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, torturer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Two Stromkirk Captains buff each other and stack bonuses on Vampires")
    void twoCaptainsStackBonuses() {
        harness.addToBattlefield(player1, new StromkirkCaptain());
        harness.addToBattlefield(player1, new StromkirkCaptain());
        Permanent torturer = harness.addToBattlefieldAndReturn(player1, new FalkenrathTorturer());

        assertThat(gqs.getEffectivePower(gd, torturer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, torturer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, torturer, Keyword.FIRST_STRIKE)).isTrue();

        List<Permanent> captains = findPermanents(player1, "Stromkirk Captain");
        assertThat(captains).hasSize(2);
        for (Permanent captain : captains) {
            assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isTrue();
        }
    }

    @Test
    @DisplayName("Vampires entering after the Captain immediately receive both bonuses")
    void buffsVampiresEnteringLater() {
        harness.addToBattlefield(player1, new StromkirkCaptain());

        Permanent torturer = harness.enterBattlefieldAndReturn(player1, new FalkenrathTorturer());

        assertThat(gqs.getEffectivePower(gd, torturer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, torturer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, torturer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Removing one of two Captains retains only the remaining Captain's bonus")
    void removingOneCaptainKeepsRemainingBonus() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StromkirkCaptain());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new StromkirkCaptain());
        Permanent torturer = harness.addToBattlefieldAndReturn(player1, new FalkenrathTorturer());

        assertThat(gqs.getEffectivePower(gd, torturer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, torturer)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, torturer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, torturer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, torturer, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FIRST_STRIKE)).isTrue();
    }
}
