package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScionOfOonaTest extends BaseCardTest {

    // ===== Static effect: buffs other Faeries you control =====

    @Test
    @DisplayName("Other Faerie creatures you control get +1/+1 and shroud")
    void buffsOtherFaeriesYouControl() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new CloudSprite());

        Permanent sprite = findPermanent(player1, "Cloud Sprite");

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Scion of Oona does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new ScionOfOona());

        Permanent scion = findPermanent(player1, "Scion of Oona");

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, scion, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Faerie creatures")
    void doesNotBuffNonFaeries() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Faerie creatures")
    void doesNotBuffOpponentFaeries() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player2, new CloudSprite());

        Permanent opponentSprite = findPermanent(player2, "Cloud Sprite");

        assertThat(gqs.getEffectivePower(gd, opponentSprite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSprite)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentSprite, Keyword.SHROUD)).isFalse();
    }

    // ===== Multiple Scions =====

    @Test
    @DisplayName("Two Scions of Oona buff each other")
    void twoScionsBuffEachOther() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new ScionOfOona());

        List<Permanent> scions = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Scion of Oona"))
                .toList();

        assertThat(scions).hasSize(2);
        for (Permanent scion : scions) {
            assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, scion, Keyword.SHROUD)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Scions give +2/+2 to other Faeries")
    void twoScionsStackBonuses() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new CloudSprite());

        Permanent sprite = findPermanent(player1, "Cloud Sprite");

        // 1/1 base + 1/1 from each Scion = 3/3
        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isTrue();
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Scion of Oona leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new CloudSprite());

        Permanent sprite = findPermanent(player1, "Cloud Sprite");

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Scion of Oona"));

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Scion of Oona resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new CloudSprite());

        Permanent sprite = findPermanent(player1, "Cloud Sprite");
        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isFalse();

        harness.setHand(player1, List.of(new ScionOfOona()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isTrue();
    }
}
