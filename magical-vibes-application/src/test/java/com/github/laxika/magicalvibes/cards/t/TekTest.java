package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TekTest extends BaseCardTest {

    @Test
    @DisplayName("Has base 2/2 characteristics without basic lands")
    void baseCharacteristics() {
        Permanent tek = addTek();

        assertThat(gqs.getEffectivePower(gd, tek)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tek)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, tek, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, tek, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, tek, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets each characteristic bonus from the matching basic land")
    void basicLandBonuses() {
        Permanent tek = addTek();
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, tek)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tek)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, tek, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, tek, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, tek, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Only basic lands controlled by Tek's controller count")
    void opponentBasicLandsDoNotCount() {
        Permanent tek = addTek();
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, tek)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tek)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, tek, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, tek, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, tek, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Bonuses update when the matching basic land leaves")
    void bonusesUpdateWhenLandLeaves() {
        Permanent tek = addTek();
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, tek)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tek)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Island"));

        assertThat(gqs.getEffectivePower(gd, tek)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tek)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, tek, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, tek, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, tek, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addTek() {
        harness.addToBattlefield(player1, new Tek());
        return findPermanent(player1, "Tek");
    }
}
