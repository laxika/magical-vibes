package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KrisMage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChoArrimAlchemist.class, CinderElemental.class, KrisMage.class})
class ChoArrimAlchemistTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next damage from the chosen source and gains that much life")
    void preventsNextDamageAndGainsLife() {
        harness.setLife(player1, 20);
        addReadyAlchemist(player1);
        Permanent cinderElemental = addReadyCinderElemental(player2);
        harness.setHand(player1, List.of(new CinderElemental()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, cinderElemental.getId());

        cinderElemental.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 22);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        harness.assertInGraveyard(player1, "Cinder Elemental");
    }

    @Test
    @DisplayName("Damage from a different source is not prevented")
    void doesNotPreventDamageFromDifferentSource() {
        harness.setLife(player1, 20);
        addReadyAlchemist(player1);
        Permanent chosenSource = addReadyCinderElemental(player2);
        Permanent otherSource = addReadyCinderElemental(player2);
        harness.setHand(player1, List.of(new CinderElemental()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        otherSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.sourceId().equals(chosenSource.getId()));
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutDiscard() {
        addReadyAlchemist(player1);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents only the next damage event from the chosen source")
    void preventsOnlyNextDamageEventFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyAlchemist(player1);
        Permanent krisMage = addReadyKrisMage(player2);
        harness.setHand(player1, List.of(new CinderElemental()));
        harness.setHand(player2, List.of(new CinderElemental(), new CinderElemental()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, krisMage.getId());

        harness.activateAbility(player2, 0, 0, null, player1.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);

        krisMage.untap();
        harness.activateAbility(player2, 0, 0, null, player1.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyAlchemist(Player player) {
        return addCreatureReady(player, new ChoArrimAlchemist());
    }

    private Permanent addReadyCinderElemental(Player player) {
        return addCreatureReady(player, new CinderElemental());
    }

    private Permanent addReadyKrisMage(Player player) {
        return addCreatureReady(player, new KrisMage());
    }
}
