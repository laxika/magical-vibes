package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.r.RazortoothRats;
import com.github.laxika.magicalvibes.cards.s.SteelGolem;
import com.github.laxika.magicalvibes.cards.s.StripedBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindStone.class, RazortoothRats.class, SpinningDarkness.class, SteelGolem.class,
        StripedBears.class})
class SpinningDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to the targeted nonblack creature and the caster gains 3 life")
    void damagesNonblackCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new StripedBears());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Striped Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Striped Bears");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A colorless creature is a legal target and takes exactly 3 damage")
    void colorlessCreatureIsLegalTarget() {
        harness.addToBattlefield(player2, new SteelGolem());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Steel Golem");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Steel Golem");
        assertThat(findPermanent(player2, "Steel Golem").getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A black creature is not a legal target")
    void blackCreatureIsIllegalTarget() {
        harness.addToBattlefield(player2, new RazortoothRats());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Razortooth Rats");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nonblack noncreature permanent is not a legal target")
    void nonCreaturePermanentIsIllegalTarget() {
        harness.addToBattlefield(player2, new MindStone());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Mind Stone");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiling the top three black cards of the graveyard casts it without paying mana")
    void alternateCostExilesTopThreeBlackCards() {
        harness.addToBattlefield(player2, new StripedBears());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.setGraveyard(player1, List.of(
                new RazortoothRats(), new RazortoothRats(), new RazortoothRats()));

        UUID targetId = harness.getPermanentId(player2, "Striped Bears");
        harness.castWithAlternateCost(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Striped Bears");
        harness.assertLife(player1, 23);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Spinning Darkness");
        assertThat(gd.exiledCards).hasSize(3);
    }

    @Test
    @DisplayName("Only the topmost black cards are exiled; nonblack cards stay in the graveyard")
    void alternateCostSkipsNonblackCards() {
        harness.addToBattlefield(player2, new StripedBears());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.setGraveyard(player1, List.of(
                new RazortoothRats(), new StripedBears(), new RazortoothRats(), new RazortoothRats()));

        UUID targetId = harness.getPermanentId(player2, "Striped Bears");
        harness.castWithAlternateCost(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Striped Bears", "Spinning Darkness");
    }

    @Test
    @DisplayName("The alternate cost is optional when the normal mana cost is paid")
    void normalCostDoesNotExileBlackGraveyardCards() {
        harness.addToBattlefield(player2, new StripedBears());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.setGraveyard(player1, List.of(
                new RazortoothRats(), new RazortoothRats(), new RazortoothRats()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Striped Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Razortooth Rats", "Razortooth Rats", "Razortooth Rats",
                        "Spinning Darkness");
    }

    @Test
    @DisplayName("The alternate cost can't be paid with fewer than three black cards in the graveyard")
    void alternateCostRequiresThreeBlackCards() {
        harness.addToBattlefield(player2, new StripedBears());
        harness.setHand(player1, List.of(new SpinningDarkness()));
        harness.setGraveyard(player1, List.of(new RazortoothRats(), new RazortoothRats()));

        UUID targetId = harness.getPermanentId(player2, "Striped Bears");

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
