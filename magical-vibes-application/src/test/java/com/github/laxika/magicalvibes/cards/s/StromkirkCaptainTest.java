package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StromkirkCaptainTest extends BaseCardTest {

    // ===== Static effect: buffs other Vampires you control =====

    @Test
    @DisplayName("Other Vampire creatures you control get +1/+1 and first strike")
    void buffsOtherVampiresYouControl() {
        harness.addToBattlefield(player1, new StromkirkCaptain());
        harness.addToBattlefield(player1, new BaronyVampire());

        Permanent barony = findPermanent(player1, "Barony Vampire");
        // Barony Vampire is 3/2 base + 1/1 from Captain = 4/3
        assertThat(gqs.getEffectivePower(gd, barony)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, barony)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, barony, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Stromkirk Captain does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new StromkirkCaptain());

        Permanent captain = findPermanent(player1, "Stromkirk Captain");
        // 2/2 base, no self-buff; innate first strike from Scryfall keywords
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(2);
        assertThat(captain.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not buff non-Vampire creatures")
    void doesNotBuffNonVampires() {
        harness.addToBattlefield(player1, new StromkirkCaptain());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Vampire creatures")
    void doesNotBuffOpponentVampires() {
        harness.addToBattlefield(player1, new StromkirkCaptain());
        harness.addToBattlefield(player2, new BaronyVampire());

        Permanent opponentBarony = findPermanent(player2, "Barony Vampire");
        assertThat(gqs.getEffectivePower(gd, opponentBarony)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentBarony)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBarony, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Bonus removed when Captain leaves =====

    @Test
    @DisplayName("Bonus is removed when Stromkirk Captain leaves the battlefield")
    void bonusRemovedWhenCaptainLeaves() {
        harness.addToBattlefield(player1, new StromkirkCaptain());
        harness.addToBattlefield(player1, new BaronyVampire());

        Permanent barony = findPermanent(player1, "Barony Vampire");
        assertThat(gqs.getEffectivePower(gd, barony)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, barony, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Stromkirk Captain"));

        assertThat(gqs.getEffectivePower(gd, barony)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, barony)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, barony, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Two Captains stack =====

    @Test
    @DisplayName("Two Stromkirk Captains buff each other and stack bonuses on Vampires")
    void twoCaptainsStackBonuses() {
        harness.addToBattlefield(player1, new StromkirkCaptain());
        harness.addToBattlefield(player1, new StromkirkCaptain());
        harness.addToBattlefield(player1, new BaronyVampire());

        Permanent barony = findPermanent(player1, "Barony Vampire");
        // 3/2 base + 2/2 from two Captains = 5/4
        assertThat(gqs.getEffectivePower(gd, barony)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, barony)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, barony, Keyword.FIRST_STRIKE)).isTrue();

        List<Permanent> captains = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Stromkirk Captain"))
                .toList();
        assertThat(captains).hasSize(2);
        for (Permanent captain : captains) {
            // 2/2 base + 1/1 from the other Captain = 3/3
            assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isTrue();
        }
    }
}
