package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisasterRadius.class, AirElemental.class, GrizzlyBears.class, HillGiant.class, Swamp.class})
class DisasterRadiusTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the revealed creature's mana value to opposing creatures")
    void damagesOpposingCreaturesBasedOnRevealedManaValue() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
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
    @DisplayName("A creature with toughness two dies when a four-mana creature is revealed")
    void usesTheRevealedManaValueForLethalDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
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

    @Test
    @DisplayName("Damages every opposing creature without damaging lands or players")
    void damagesEveryOpposingCreatureOnly() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new DisasterRadius(), new GrizzlyBears()));
        addMana();

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
        assertThat(land.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Uses the revealed mana value even if the card leaves hand before resolution")
    void retainsRevealedManaValueAfterCardLeavesHand() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DisasterRadius(), new GrizzlyBears()));
        addMana();

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(opponent.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast with no opposing creatures and still reveals the chosen card")
    void resolvesWithoutOpposingCreatures() {
        GrizzlyBears revealed = new GrizzlyBears();
        harness.setHand(player1, List.of(revealed, new DisasterRadius()));
        addMana();

        harness.castSorceryWithDiscard(player1, 1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Disaster Radius");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
