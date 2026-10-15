package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.InitiatesCompanion;
import com.github.laxika.magicalvibes.cards.k.KefnetTheMindful;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheShire.class, KefnetTheMindful.class, InitiatesCompanion.class})
class TheShireTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without a legendary creature")
    void entersTappedWithoutLegendaryCreature() {
        playLand();

        assertThat(findPermanent(player1, "The Shire").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a legendary creature")
    void entersUntappedWithLegendaryCreature() {
        harness.addToBattlefield(player1, new KefnetTheMindful());
        playLand();

        assertThat(findPermanent(player1, "The Shire").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps for green mana")
    void tapsForGreenMana() {
        Permanent shire = addReadyShire();

        harness.activateAbility(player1, indexOf(shire), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(shire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an untapped creature to create a Food token")
    void tapsCreatureToCreateFoodToken() {
        Permanent shire = addReadyShire();
        Permanent bears = addCreatureReady(player1, new InitiatesCompanion());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, indexOf(shire), 1, null, null);
        harness.passBothPriorities();

        assertThat(shire.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot create Food without an untapped creature")
    void cannotCreateFoodWithoutUntappedCreature() {
        Permanent shire = addReadyShire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(shire), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    void entersTappedWithOnlyNonlegendaryCreature() {
        harness.addToBattlefield(player1, new InitiatesCompanion());
        playLand();

        assertThat(findPermanent(player1, "The Shire").isTapped()).isTrue();
    }

    @Test
    void opponentsLegendaryCreatureDoesNotAllowUntappedEntry() {
        harness.addToBattlefield(player2, new KefnetTheMindful());
        playLand();

        assertThat(findPermanent(player1, "The Shire").isTapped()).isTrue();
    }

    @Test
    void tappedLegendaryCreatureAllowsUntappedEntry() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KefnetTheMindful());
        creature.tap();
        playLand();

        assertThat(findPermanent(player1, "The Shire").isTapped()).isFalse();
    }

    @Test
    void canTapSummoningSickCreatureAndUseCreatedFood() {
        Permanent shire = addReadyShire();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new InitiatesCompanion());
        creature.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, indexOf(shire), 1, null, null);

        assertThat(creature.isTapped()).isTrue();
        assertThat(shire.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.passBothPriorities();

        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(food), 0, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void cannotTapOpponentsCreatureForFood() {
        Permanent shire = addReadyShire();
        Permanent creature = addCreatureReady(player2, new InitiatesCompanion());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(shire), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(creature.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void cannotTapAlreadyTappedCreatureForFood() {
        Permanent shire = addReadyShire();
        Permanent creature = addCreatureReady(player1, new InitiatesCompanion());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(shire), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private void playLand() {
        harness.setHand(player1, List.of(new TheShire()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyShire() {
        return harness.addToBattlefieldAndReturn(player1, new TheShire());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
