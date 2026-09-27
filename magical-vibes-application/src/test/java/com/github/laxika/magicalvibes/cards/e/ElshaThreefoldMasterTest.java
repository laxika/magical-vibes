package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElshaThreefoldMaster.class, Shock.class})
class ElshaThreefoldMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates Monk tokens equal to combat damage dealt to a player")
    void createsMonksEqualToCombatDamage() {
        Permanent elsha = addCreatureReady(player1, new ElshaThreefoldMaster());
        elsha.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Monk")).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Elsha and the created Monk have prowess")
    void elshaAndCreatedMonkHaveProwess() {
        Permanent elsha = addCreatureReady(player1, new ElshaThreefoldMaster());
        elsha.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        Permanent monk = findPermanent(player1, "Monk");

        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elsha, Keyword.PROWESS)).isTrue();
        assertThat(gqs.hasKeyword(gd, monk, Keyword.PROWESS)).isTrue();
        assertThat(gqs.getEffectivePower(gd, elsha)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
    }
}
