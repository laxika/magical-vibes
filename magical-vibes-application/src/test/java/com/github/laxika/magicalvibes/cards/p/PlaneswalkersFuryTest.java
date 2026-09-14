package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CrosissCatacombs;
import com.github.laxika.magicalvibes.cards.g.Gainsay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlaneswalkersFury.class, Gainsay.class, CrosissCatacombs.class})
class PlaneswalkersFuryTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToRevealedManaValue() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFury());
        Gainsay revealed = new Gainsay();
        harness.setHand(player2, List.of(revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    void emptyHandDoesNotDealDamage() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFury());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void landHasZeroManaValue() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFury());
        CrosissCatacombs land = new CrosissCatacombs();
        harness.setHand(player2, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void onlyTargetsOpponents() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFury());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresThreeGenericAndOneRedMana() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFury());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyActivatesAtSorcerySpeed() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFury());
        harness.setHand(player2, List.of(new Gainsay()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }
}
