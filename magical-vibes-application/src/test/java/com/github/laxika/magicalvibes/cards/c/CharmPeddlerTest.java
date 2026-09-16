package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.j.JeweledTorque;
import com.github.laxika.magicalvibes.cards.w.Warmonger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharmPeddler.class, Warmonger.class, JeweledTorque.class})
class CharmPeddlerTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next damage from the chosen source to the target creature")
    void preventsNextDamageFromChosenSource() {
        Permanent peddler = addCreatureReady(player1, new CharmPeddler());
        Permanent source = addCreatureReady(player1, new Warmonger());
        Permanent target = addCreatureReady(player2, new Warmonger());
        harness.setHand(player1, List.of(new Warmonger()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(peddler.isTapped()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(source.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Warmonger");

        // The shield prevents only the next matching damage event.
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, null);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage from a different source is not prevented")
    void doesNotPreventDamageFromDifferentSource() {
        addCreatureReady(player1, new CharmPeddler());
        Permanent chosenSource = addCreatureReady(player1, new Warmonger());
        Permanent otherSource = addCreatureReady(player1, new Warmonger());
        Permanent target = addCreatureReady(player2, new Warmonger());
        harness.setHand(player1, List.of(new Warmonger()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(otherSource), null, null);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutDiscard() {
        addCreatureReady(player1, new CharmPeddler());
        harness.setHand(player1, List.of());
        Permanent target = addCreatureReady(player2, new Warmonger());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new CharmPeddler());
        harness.setHand(player1, List.of(new Warmonger()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent torque = harness.addToBattlefieldAndReturn(player2, new JeweledTorque());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, torque.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
