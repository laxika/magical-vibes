package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SonicSeizure.class, SengirVampire.class})
class SonicSeizureTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a random card as a cost and deals 3 damage to any target")
    void discardsRandomCardAndDealsDamage() {
        harness.setHand(player1, List.of(new SonicSeizure(), new SengirVampire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Sengir Vampire");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals exactly 3 damage to a creature target")
    void dealsThreeDamageToCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        harness.setHand(player1, List.of(new SonicSeizure(), new SengirVampire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Sengir Vampire");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot be cast when there is no other card to discard")
    void cannotCastWithoutCardToDiscard() {
        harness.setHand(player1, List.of(new SonicSeizure()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card at random");
    }

    @Test
    @DisplayName("Pays exactly one random discard before resolving, excluding the spell itself")
    void paysRandomDiscardBeforeResolution() {
        SonicSeizure spell = new SonicSeizure();
        SengirVampire first = new SengirVampire();
        SengirVampire second = new SengirVampire();
        harness.setHand(player1, List.of(first, spell, second));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 1, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .containsAnyOf(first, second).doesNotContain(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1)
                .containsAnyOf(first, second).doesNotContain(spell);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(spell);
    }

    @Test
    @DisplayName("Can target its own controller")
    void canDamageItsController() {
        harness.setHand(player1, List.of(new SonicSeizure(), new SengirVampire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not refund the discard when its creature target leaves before resolution")
    void discardRemainsPaidWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        harness.setHand(player1, List.of(new SonicSeizure(), new SengirVampire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player1, "Sengir Vampire");

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sengir Vampire");
        harness.assertInGraveyard(player1, "Sonic Seizure");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
