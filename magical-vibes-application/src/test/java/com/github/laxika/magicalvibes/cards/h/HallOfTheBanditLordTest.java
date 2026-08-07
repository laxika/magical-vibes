package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HallOfTheBanditLordTest extends BaseCardTest {

    @Test
    @DisplayName("Hall of the Bandit Lord enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new HallOfTheBanditLord()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Hall of the Bandit Lord").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping and paying 3 life adds {C}")
    void activatingAddsColorlessAndPaysLife() {
        Permanent hall = harness.addToBattlefieldAndReturn(player1, new HallOfTheBanditLord());
        hall.untap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, 17);
        assertThat(gd.stack).isEmpty();
        assertThat(hall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature spell paid for with Hall's mana gains haste")
    void creatureCastWithHallManaGainsHaste() {
        Permanent hall = harness.addToBattlefieldAndReturn(player1, new HallOfTheBanditLord());
        hall.untap();
        harness.activateAbility(player1, 0, null, null);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A creature spell paid for with ordinary mana does not gain haste")
    void creatureCastWithOrdinaryManaHasNoHaste() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.HASTE)).isFalse();
    }
}
