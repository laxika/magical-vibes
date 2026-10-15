package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({Thornado.class, AirElemental.class, GrizzlyBears.class})
class ThornadoTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature with flying")
    void destroysTargetCreatureWithFlying() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AirElemental()).getId();

        harness.setHand(player1, List.of(new Thornado()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new Thornado()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Thornado and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Thornado()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thornado");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy your own creature with flying")
    void destroysOwnFlyingCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new AirElemental()).getId();
        harness.setHand(player1, List.of(new Thornado()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Cycling discards immediately but draws only on resolution")
    void cyclingPaysDiscardBeforeDrawing() {
        harness.setHand(player1, List.of(new Thornado()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Thornado");
        harness.assertInGraveyard(player1, "Thornado");
        harness.assertNotInHand(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling requires green mana and does not discard when payment fails")
    void cyclingRequiresGreenMana() {
        harness.setHand(player1, List.of(new Thornado()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Thornado");
        harness.assertNotInGraveyard(player1, "Thornado");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }
}

@CardUsed({Thornado.class, AirElemental.class, GrizzlyBears.class})
class Mh1ThornadoTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature with flying")
    void destroysTargetCreatureWithFlying() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AirElemental()).getId();

        harness.setHand(player1, List.of(new Thornado()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new Thornado()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Cycling discards Thornado and draws a card")
    void cyclingDiscardsAndDraws() {
        harness.setHand(player1, List.of(new Thornado()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thornado");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
