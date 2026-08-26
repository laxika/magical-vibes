package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MuYanlingSkyDancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LithomanticBarrage.class, Cancel.class, EliteVanguard.class, GrizzlyBears.class,
        MuYanlingSkyDancer.class})
class LithomanticBarrageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a creature that is neither white nor blue")
    void dealsOneDamageToOtherColor() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 5 damage to a white creature")
    void dealsFiveDamageToWhiteCreature() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Elite Vanguard"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Deals 5 damage to a blue planeswalker")
    void dealsFiveDamageToBluePlaneswalker() {
        Permanent planeswalker = new Permanent(new MuYanlingSkyDancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        LithomanticBarrage barrage = new LithomanticBarrage();
        harness.setHand(player1, List.of(barrage));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        LithomanticBarrage barrage = new LithomanticBarrage();
        harness.setHand(player1, List.of(barrage));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, barrage.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Cancel");
    }
}
