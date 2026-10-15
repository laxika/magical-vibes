package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProphetOfDistortion.class, GrizzlyBears.class})
class ProphetOfDistortionTest extends BaseCardTest {

    @Test
    @DisplayName("Pays three generic and one colorless mana to draw a card")
    void paysAbilityCostAndDrawsCard() {
        addReadyProphet();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot pay the colorless symbol with colored mana")
    void requiresColorlessMana() {
        addReadyProphet();
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate repeatedly while tapped and summoning sick")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent prophet = harness.addToBattlefieldAndReturn(player1, new ProphetOfDistortion());
        prophet.setSummoningSick(true);
        prophet.tap();
        harness.setHand(player1, List.of());
        ProphetOfDistortion firstCard = new ProphetOfDistortion();
        ProphetOfDistortion secondCard = new ProphetOfDistortion();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(prophet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can pay the entire ability cost with colorless mana")
    void canPayEntireCostWithColorlessMana() {
        harness.addToBattlefield(player1, new ProphetOfDistortion());
        harness.setHand(player1, List.of());
        ProphetOfDistortion drawnCard = new ProphetOfDistortion();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Colorless mana alone does not waive the generic cost")
    void requiresEnoughTotalMana() {
        harness.addToBattlefield(player1, new ProphetOfDistortion());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void addReadyProphet() {
        Permanent prophet = harness.addToBattlefieldAndReturn(player1, new ProphetOfDistortion());
        prophet.setSummoningSick(false);
    }
}
