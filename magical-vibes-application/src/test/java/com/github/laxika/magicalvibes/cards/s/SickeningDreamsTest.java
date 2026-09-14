package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SickeningDreams.class, AvenTrooper.class, AngelOfRetribution.class})
class SickeningDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Discards X cards and deals X damage to each creature and player")
    void discardsXAndDealsDamageToCreaturesAndPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AvenTrooper());
        Permanent angelOfRetribution = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        harness.setHand(player1, new ArrayList<>(List.of(
                new SickeningDreams(), new AvenTrooper(), new AvenTrooper())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithDiscards(player1, 0, 2, (UUID) null, List.of(1, 2));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Aven Trooper");
        assertThat(angelOfRetribution.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder(
                        "Sickening Dreams", "Aven Trooper", "Aven Trooper", "Aven Trooper");
    }

    @Test
    @DisplayName("X=0 discards no cards and deals no damage")
    void zeroXDoesNothing() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AvenTrooper());
        Permanent angelOfRetribution = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        harness.setHand(player1, new ArrayList<>(List.of(new SickeningDreams(), new AvenTrooper())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithDiscards(player1, 0, 0, (UUID) null, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Aven Trooper");
        assertThat(angelOfRetribution.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Aven Trooper");
    }

    @Test
    @DisplayName("Casting is rejected when the hand cannot cover X discards")
    void cannotCastWithoutEnoughCardsToDiscard() {
        harness.setHand(player1, new ArrayList<>(List.of(new SickeningDreams(), new AvenTrooper())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 2, (UUID) null, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard 2");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
