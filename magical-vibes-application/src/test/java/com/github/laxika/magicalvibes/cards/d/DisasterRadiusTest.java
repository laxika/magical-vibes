package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisasterRadiusTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the revealed creature's mana value to opposing creatures")
    void damagesOpposingCreaturesBasedOnRevealedManaValue() {
        Permanent ownCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentCreature = addReadyCreature(player2, new AirElemental());
        GrizzlyBears revealed = new GrizzlyBears();
        harness.setHand(player1, List.of(new DisasterRadius(), revealed));
        addMana();

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("A creature with toughness two dies when a three-mana creature is revealed")
    void usesTheRevealedManaValueForLethalDamage() {
        addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DisasterRadius(), new HillGiant()));
        addMana();

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot be cast without a creature card to reveal")
    void requiresCreatureCardToReveal() {
        harness.setHand(player1, List.of(new DisasterRadius(), new Swamp()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(7);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
