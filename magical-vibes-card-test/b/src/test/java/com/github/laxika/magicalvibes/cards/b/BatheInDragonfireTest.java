package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArashinWarBeast;
import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.cards.t.ThornwoodFalls;
import com.github.laxika.magicalvibes.cards.w.WhispererOfTheWilds;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BatheInDragonfire.class, ArashinWarBeast.class, WhispererOfTheWilds.class,
        FeralKrushok.class, ThornwoodFalls.class})
class BatheInDragonfireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void deals4DamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArashinWarBeast());
        castBatheInDragonfire(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Arashin War Beast");
    }

    @Test
    @DisplayName("Deals lethal damage to a small creature")
    void dealsLethalDamageToSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WhispererOfTheWilds());
        castBatheInDragonfire(target);

        harness.assertNotOnBattlefield(player2, "Whisperer of the Wilds");
        harness.assertInGraveyard(player2, "Whisperer of the Wilds");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOwnCreatureWithoutDamagingOtherCreaturesOrPlayers() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArashinWarBeast());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ArashinWarBeast());

        castBatheInDragonfire(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(other.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Bathe in Dragonfire");
    }

    @Test
    void fourDamageIsLethalToCreatureWithExactlyFourToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FeralKrushok());

        castBatheInDragonfire(target);

        harness.assertNotOnBattlefield(player2, "Feral Krushok");
        harness.assertInGraveyard(player2, "Feral Krushok");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ThornwoodFalls());
        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotDealDamageWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WhispererOfTheWilds());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ArashinWarBeast());
        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(other.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Bathe in Dragonfire");
        assertThat(gd.stack).isEmpty();
    }

    private void castBatheInDragonfire(Permanent target) {
        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
