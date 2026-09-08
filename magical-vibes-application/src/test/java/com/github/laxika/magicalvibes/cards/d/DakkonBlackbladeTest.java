package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DakkonBlackbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Dakkon's power and toughness equal the number of lands you control")
    void ptEqualsControlledLands() {
        Permanent dakkon = addDakkonReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, dakkon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dakkon)).isEqualTo(3);
    }

    @Test
    @DisplayName("Dakkon counts only his controller's lands")
    void countsOnlyControllersLands() {
        Permanent dakkon = addDakkonReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, dakkon)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dakkon)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dakkon's power and toughness update when lands change")
    void ptUpdatesWhenLandsChange() {
        Permanent dakkon = addDakkonReady(player1);

        assertThat(gqs.getEffectivePower(gd, dakkon)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, dakkon)).isZero();

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        assertThat(gqs.getEffectivePower(gd, dakkon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dakkon)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gqs.getEffectivePower(gd, dakkon)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, dakkon)).isZero();
    }

    private Permanent addDakkonReady(Player player) {
        Permanent permanent = new Permanent(new DakkonBlackblade());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
