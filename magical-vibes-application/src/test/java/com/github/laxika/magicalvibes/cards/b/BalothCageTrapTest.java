package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({BalothCageTrap.class, Ornithopter.class, GrizzlyBears.class})
class BalothCageTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/4 green Beast creature token")
    void createsBeastToken() {
        harness.setHand(player1, List.of(new BalothCageTrap()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).contains(CardSubtype.BEAST);
        assertThat(beast.getEffectivePower()).isEqualTo(4);
        assertThat(beast.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can be cast for {1}{G} after an opponent artifact entered this turn")
    void castsForAlternateCostAfterOpponentArtifactEntered() {
        harness.enterBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new BalothCageTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
    }

    @Test
    @DisplayName("The alternate cost requires an opponent artifact to have entered this turn")
    void alternateCostRequiresOpponentArtifact() {
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BalothCageTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Your own artifact entering does not enable the alternate cost")
    void ownArtifactDoesNotEnableAlternateCost() {
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new BalothCageTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An artifact already on the battlefield does not enable the alternate cost")
    void existingArtifactDoesNotEnableAlternateCost() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new BalothCageTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The alternate cost remains available after the opponent's artifact dies")
    void alternateCostRemainsAvailableAfterArtifactDies() {
        Permanent artifact = harness.enterBattlefieldAndReturn(player2, new Ornithopter());
        artifact.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.setHand(player1, List.of(new BalothCageTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
    }

    @Test
    @DisplayName("The normal mana cost may still be paid when the alternate cost is available")
    void normalCostRemainsAvailable() {
        harness.enterBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new BalothCageTrap()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
        assertThat(countPermanents(player2, "Beast")).isZero();
    }
}
