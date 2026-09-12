package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaprolingCluster.class, SaprolingBurst.class})
class SaprolingClusterTest extends BaseCardTest {

    @Test
    @DisplayName("Controller pays {1} and discards a card to create a Saproling")
    void controllerActivatesAbility() {
        harness.addToBattlefield(player1, new SaprolingCluster());
        harness.setHand(player1, List.of(new SaprolingBurst()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Saproling Burst");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        List<Permanent> saprolings = findPermanents(player1, "Saproling");
        assertThat(saprolings).hasSize(1);
        Permanent saproling = saprolings.getFirst();
        assertThat(saproling.getCard().getColors()).containsExactly(CardColor.GREEN);
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent may pay {1} and discard a card to create a Saproling")
    void opponentActivatesAbility() {
        harness.addToBattlefield(player1, new SaprolingCluster());
        harness.setHand(player2, List.of(new SaprolingBurst()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Saproling Burst");
        harness.assertOnBattlefield(player2, "Saproling");
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("The ability cannot be activated without paying {1}")
    void requiresManaToActivate() {
        harness.addToBattlefield(player1, new SaprolingCluster());
        harness.setHand(player1, List.of(new SaprolingBurst()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Saproling Burst");
    }

    @Test
    @DisplayName("The activating player chooses which card to discard")
    void discardsChosenCard() {
        harness.addToBattlefield(player1, new SaprolingCluster());
        harness.setHand(player1, List.of(new SaprolingBurst(), new SaprolingCluster()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Saproling Cluster");
        harness.assertInHand(player1, "Saproling Burst");
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void requiresCardToDiscard() {
        harness.addToBattlefield(player1, new SaprolingCluster());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
