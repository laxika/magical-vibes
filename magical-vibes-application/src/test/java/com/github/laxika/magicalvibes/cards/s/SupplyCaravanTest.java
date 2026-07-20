package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SupplyCaravanTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 white Warrior with vigilance when you control a tapped creature")
    void createsWarriorWhenControllingTappedCreature() {
        Permanent tappedBears = new Permanent(new GrizzlyBears());
        tappedBears.setSummoningSick(false);
        tappedBears.tap();
        gd.playerBattlefields.get(player1.getId()).add(tappedBears);

        harness.setHand(player1, List.of(new SupplyCaravan()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Warrior"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creates no token when you control no tapped creature")
    void createsNoTokenWithoutTappedCreature() {
        Permanent untappedBears = new Permanent(new GrizzlyBears());
        untappedBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(untappedBears);

        harness.setHand(player1, List.of(new SupplyCaravan()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Warrior"));
    }
}
