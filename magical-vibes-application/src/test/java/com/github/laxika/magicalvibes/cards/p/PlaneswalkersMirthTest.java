package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.cards.v.VoiceOfAll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlaneswalkersMirth.class, VoiceOfAll.class, ForsakenCity.class})
class PlaneswalkersMirthTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the mana value of the randomly revealed card")
    void gainsLifeEqualToRevealedManaValue() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersMirth());
        VoiceOfAll revealed = new VoiceOfAll();
        harness.setHand(player2, List.of(revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("Does nothing when the target opponent's hand is empty")
    void emptyHandNoLifeGain() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersMirth());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersMirth());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be activated outside a main phase")
    void canBeActivatedAtInstantSpeed() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersMirth());
        harness.setHand(player2, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Revealing a land gains no life")
    void landHasZeroManaValue() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersMirth());
        ForsakenCity land = new ForsakenCity();
        harness.setHand(player2, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
    }
}
