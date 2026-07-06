package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChapelGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DrogskolCaptainTest extends BaseCardTest {

    // ===== Static effect: buffs other Spirits you control =====

    @Test
    @DisplayName("Other Spirit creatures you control get +1/+1 and hexproof")
    void buffsOtherSpiritsYouControl() {
        harness.addToBattlefield(player1, new DrogskolCaptain());
        harness.addToBattlefield(player1, new ChapelGeist());

        Permanent geist = findPermanent(player1, "Chapel Geist");

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, geist, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Drogskol Captain does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new DrogskolCaptain());

        Permanent captain = findPermanent(player1, "Drogskol Captain");

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, captain, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Spirit creatures")
    void doesNotBuffNonSpirits() {
        harness.addToBattlefield(player1, new DrogskolCaptain());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Spirit creatures")
    void doesNotBuffOpponentSpirits() {
        harness.addToBattlefield(player1, new DrogskolCaptain());
        harness.addToBattlefield(player2, new ChapelGeist());

        Permanent opponentGeist = findPermanent(player2, "Chapel Geist");

        assertThat(gqs.getEffectivePower(gd, opponentGeist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentGeist)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentGeist, Keyword.HEXPROOF)).isFalse();
    }

    // ===== Multiple Drogskol Captains =====

    @Test
    @DisplayName("Two Drogskol Captains buff each other")
    void twoCaptainsBuffEachOther() {
        harness.addToBattlefield(player1, new DrogskolCaptain());
        harness.addToBattlefield(player1, new DrogskolCaptain());

        List<Permanent> captains = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drogskol Captain"))
                .toList();

        assertThat(captains).hasSize(2);
        for (Permanent captain : captains) {
            assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, captain, Keyword.HEXPROOF)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Drogskol Captains give +2/+2 to other Spirits")
    void twoCaptainsStackBonuses() {
        harness.addToBattlefield(player1, new DrogskolCaptain());
        harness.addToBattlefield(player1, new DrogskolCaptain());
        harness.addToBattlefield(player1, new ChapelGeist());

        Permanent geist = findPermanent(player1, "Chapel Geist");

        // 2/3 base + 2/2 from two captains = 4/5
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, geist, Keyword.HEXPROOF)).isTrue();
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Drogskol Captain leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new DrogskolCaptain());
        harness.addToBattlefield(player1, new ChapelGeist());

        Permanent geist = findPermanent(player1, "Chapel Geist");

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, geist, Keyword.HEXPROOF)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Drogskol Captain"));

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, geist, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Drogskol Captain resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new ChapelGeist());

        Permanent geist = findPermanent(player1, "Chapel Geist");
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, geist, Keyword.HEXPROOF)).isFalse();

        harness.setHand(player1, List.of(new DrogskolCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, geist, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new DrogskolCaptain());
        harness.addToBattlefield(player1, new ChapelGeist());

        Permanent geist = findPermanent(player1, "Chapel Geist");

        geist.setPowerModifier(geist.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(8); // 2 base + 5 spell + 1 static

        geist.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, geist, Keyword.HEXPROOF)).isTrue();
    }
}
