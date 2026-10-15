package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnchovyBananaPizza.class, GrizzlyBears.class, Forest.class})
class AnchovyBananaPizzaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield destroys the targeted creature")
    void enteringTheBattlefieldDestroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AnchovyBananaPizza()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Anchovy & Banana Pizza");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The enters-the-battlefield ability can target only a creature")
    void etbCannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AnchovyBananaPizza()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Sacrificing it gains 3 life")
    void sacrificingItGainsThreeLife() {
        harness.addToBattlefield(player1, new AnchovyBananaPizza());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Anchovy & Banana Pizza");
    }

    @Test
    @DisplayName("A tapped Food cannot pay the activation's tap cost")
    void tappedFoodCannotBeActivated() {
        harness.addToBattlefieldAndReturn(player1, new AnchovyBananaPizza()).tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Anchovy & Banana Pizza");
        harness.assertNotInGraveyard(player1, "Anchovy & Banana Pizza");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Insufficient mana prevents activation and sacrifice")
    void insufficientManaDoesNotSacrificeFood() {
        harness.addToBattlefield(player1, new AnchovyBananaPizza());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Anchovy & Banana Pizza");
        harness.assertNotInGraveyard(player1, "Anchovy & Banana Pizza");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The activating player gains the life")
    void opponentActivatingFoodGainsLife() {
        harness.addToBattlefield(player2, new AnchovyBananaPizza());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
        harness.assertInGraveyard(player2, "Anchovy & Banana Pizza");
    }

    @Test
    @DisplayName("Sacrifice is paid immediately but life is gained on resolution")
    void sacrificeIsCostAndLifeGainUsesStack() {
        harness.addToBattlefield(player1, new AnchovyBananaPizza());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Anchovy & Banana Pizza");
        harness.assertInGraveyard(player1, "Anchovy & Banana Pizza");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The artifact can resolve when there are no creatures to target")
    void resolvesWithoutAnyCreature() {
        harness.setHand(player1, List.of(new AnchovyBananaPizza()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Anchovy & Banana Pizza");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
