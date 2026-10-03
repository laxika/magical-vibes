package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.f.FesteringNewt;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BubblingCauldron.class, FesteringNewt.class, ElvishMystic.class, OneWithTheStars.class})
class BubblingCauldronTest extends BaseCardTest {

    private void setUpMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Sacrificing a creature gains 4 life")
    void sacrificeCreatureGainsFourLife() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        harness.addToBattlefield(player1, new ElvishMystic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Elvish Mystic");
    }

    @Test
    @DisplayName("Cannot activate life-gain ability without a creature to sacrifice")
    void cannotGainLifeWithoutCreature() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing a Festering Newt drains each opponent for 4 and gains that much life")
    void sacrificeNewtDrainsOpponent() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Festering Newt");
    }

    @Test
    @DisplayName("Cannot activate Newt ability by sacrificing a differently named creature")
    void cannotSacrificeNonNewtForDrain() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        harness.addToBattlefield(player1, new ElvishMystic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice and tap are paid before the life gain resolves")
    void paysCostsBeforeResolvingLifeGain() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        harness.addToBattlefield(player1, new ElvishMystic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Elvish Mystic");
        harness.assertLife(player1, 20);
        assertThat(findPermanent(player1, "Bubbling Cauldron").isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("A Newt that is no longer a creature cannot pay the drain cost")
    void cannotSacrificeNoncreatureNewt() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        var newt = harness.addToBattlefieldAndReturn(player1, new FesteringNewt());
        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, newt.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, newt)).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither ability can sacrifice an opponent's Newt")
    void cannotSacrificeOpponentsNewt() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        harness.addToBattlefield(player2, new FesteringNewt());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both abilities require an untapped Cauldron")
    void cannotActivateTappedCauldron() {
        setUpMain();
        var cauldron = harness.addToBattlefieldAndReturn(player1, new BubblingCauldron());
        cauldron.tap();
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both abilities require one mana")
    void cannotActivateWithoutMana() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        harness.addToBattlefield(player1, new FesteringNewt());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
