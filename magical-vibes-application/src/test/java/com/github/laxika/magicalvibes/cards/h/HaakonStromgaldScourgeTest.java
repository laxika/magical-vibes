package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.c.ChillToTheBone;
import com.github.laxika.magicalvibes.cards.w.WhiteShieldCrusader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaakonStromgaldScourge.class, WhiteShieldCrusader.class, BorealDruid.class,
        ChillToTheBone.class})
class HaakonStromgaldScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be cast from hand")
    void cannotBeCastFromHand() {
        harness.setHand(player1, List.of(new HaakonStromgaldScourge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from hand");
    }

    @Test
    @DisplayName("Can be cast from graveyard")
    void canBeCastFromGraveyard() {
        harness.setGraveyard(player1, List.of(new HaakonStromgaldScourge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Haakon, Stromgald Scourge");
    }

    @Test
    @DisplayName("Allows Knight spells to be cast from the controller's graveyard")
    void allowsKnightSpellsFromGraveyard() {
        harness.addToBattlefield(player1, new HaakonStromgaldScourge());
        harness.setGraveyard(player1, List.of(new WhiteShieldCrusader()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "White Shield Crusader");
    }

    @Test
    @DisplayName("Does not allow non-Knight spells to be cast from the graveyard")
    void doesNotAllowNonKnightSpellsFromGraveyard() {
        harness.addToBattlefield(player1, new HaakonStromgaldScourge());
        harness.setGraveyard(player1, List.of(new BorealDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Does not allow Knight spells from the graveyard without Haakon")
    void doesNotAllowKnightSpellsFromGraveyardWithoutHaakon() {
        harness.setGraveyard(player1, List.of(new WhiteShieldCrusader()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Does not grant the opponent permission to cast Knight spells from their graveyard")
    void doesNotAllowOpponentToCastKnightSpellsFromTheirGraveyard() {
        harness.addToBattlefield(player1, new HaakonStromgaldScourge());
        harness.setGraveyard(player2, List.of(new WhiteShieldCrusader()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Controller loses 2 life when Haakon dies")
    void controllerLosesLifeWhenHaakonDies() {
        Permanent haakon = harness.addToBattlefieldAndReturn(player1, new HaakonStromgaldScourge());
        harness.setHand(player2, List.of(new ChillToTheBone()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, haakon.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Haakon, Stromgald Scourge");
    }

    @Test
    @DisplayName("Knight casting permission ends when Haakon dies")
    void knightCastingPermissionEndsWhenHaakonDies() {
        Permanent haakon = harness.addToBattlefieldAndReturn(player1, new HaakonStromgaldScourge());
        harness.setGraveyard(player1, List.of(new WhiteShieldCrusader()));
        harness.setHand(player2, List.of(new ChillToTheBone()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, haakon.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
        harness.assertInGraveyard(player1, "White Shield Crusader");
    }

    @Test
    @DisplayName("Haakon can be cast again after dying without another Haakon on the battlefield")
    void canBeCastAgainAfterDying() {
        Permanent haakon = harness.addToBattlefieldAndReturn(player1, new HaakonStromgaldScourge());
        harness.setHand(player2, List.of(new ChillToTheBone()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, haakon.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Haakon, Stromgald Scourge");
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Haakon, Stromgald Scourge");
        harness.assertNotInGraveyard(player1, "Haakon, Stromgald Scourge");
        harness.assertLife(player1, 18);
    }
}
