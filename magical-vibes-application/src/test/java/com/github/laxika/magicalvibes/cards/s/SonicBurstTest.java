package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WallOfNets;
import com.github.laxika.magicalvibes.cards.w.WoodElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SonicBurst.class, WoodElves.class, WallOfNets.class})
class SonicBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a random card as a cost and deals 4 damage to any target")
    void discardsRandomCardAndDealsDamage() {
        harness.setHand(player1, List.of(new SonicBurst(), new WoodElves()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Wood Elves");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Deals exactly 4 damage to a creature target")
    void dealsFourDamageToCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfNets());
        harness.setHand(player1, List.of(new SonicBurst(), new WoodElves()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Wall of Nets");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Discard is paid before damage resolves")
    void paysDiscardBeforeResolution() {
        harness.setHand(player1, List.of(new SonicBurst(), new WoodElves()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Wood Elves");
        harness.assertNotInGraveyard(player1, "Sonic Burst");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Sonic Burst");
    }

    @Test
    @DisplayName("Discards exactly one other card from a larger hand")
    void discardsExactlyOneCardFromLargerHand() {
        WoodElves elves = new WoodElves();
        WallOfNets wall = new WallOfNets();
        harness.setHand(player1, List.of(new SonicBurst(), elves, wall));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isIn(elves, wall);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst()).isIn(elves, wall);
        assertThat(gd.playerHands.get(player1.getId()).getFirst())
                .isNotSameAs(gd.playerGraveyards.get(player1.getId()).getFirst());

        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertInGraveyard(player1, "Sonic Burst");
    }

    @Test
    @DisplayName("Cannot be cast when there is no other card to discard")
    void cannotCastWithoutCardToDiscard() {
        harness.setHand(player1, List.of(new SonicBurst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card at random");
    }
}
